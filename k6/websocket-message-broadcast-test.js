import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep } from 'k6';


export const options = {
    vus: 5,
    iterations: 5
};

export function setup() {
    const username = "k6test-user1";

    const password = "password";

    const res = http.post(
        "http://localhost:8080/api/auth/register",
        JSON.stringify({
            username,
            password
        }),
        {
            headers: {
                "Content-Type": "application/json",
            },
        }
    );

    return {
        username,
        password,
    };
}


export default function (data) {

    // Login
    const loginRes = http.post(
        "http://localhost:8080/api/auth/login",
        JSON.stringify({
            username: data.username,
            password: data.password,
        }),
        {
            headers: {
                "Content-Type": "application/json",
            },
        }
    );

    check(loginRes, {
        "login success":
            (r) => r.status === 200
    });

    const accessToken =
        loginRes.cookies.accessToken[0].value;

    // WebSocket
    const wsUrl =
        "ws://localhost:8080/ws/board?boardId=1";


    const res = ws.connect(
        wsUrl,
        {
            headers: {
                Cookie:
                    `accessToken=${accessToken}`
            }
        },
        function (socket) {

            socket.on(
                "open",
                function () {
                    console.log(
                        "connected VU=",
                        __VU
                    );

                    // join
                    socket.send(
                        JSON.stringify({
                            type: "join",
                            boardId: "1"
                        })
                    );

                    // stroke送信開始
                    let count = 0;

                    socket.setInterval(
                        function () {
                            count++;

                            socket.send(
                                JSON.stringify({

                                    type: "stroke:add",

                                    boardId: "1",

                                    stroke: {
                                        points: [
                                            {
                                                x: 100 + count,
                                                y: 100 + count
                                            },
                                            {
                                                x: 200 + count,
                                                y: 200 + count
                                            }
                                        ],

                                        color: "#000000",
                                        width: 3
                                    }

                                })
                            );

                        },
                        100
                    );
                    // 100ms = 10回/sec
                }
            );

            socket.on(
                "message",
                function (message) {
                    // 受信確認
                    // console.log(message);
                }
            );

            socket.on(
                "close",
                function () {
                    console.log(
                        "closed VU=",
                        __VU
                    );
                }
            );

            // 10秒維持
            socket.setTimeout(
                function () {

                    socket.close();

                },
                10000
            );
        }
    );


    check(res, {
        "websocket handshake":
            (r) => r.status === 101
    });

    sleep(1);

}