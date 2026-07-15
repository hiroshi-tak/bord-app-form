import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep } from 'k6';


export const options = {
    vus: 6,
    iterations: 6
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

    // login
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


    const res = ws.connect(
        "ws://localhost:8080/ws/board?boardId=1",
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

                    socket.send(
                        JSON.stringify({

                            type: "join",

                            boardId: "1"

                        })
                    );
                }
            );


            socket.on(
                "message",
                function (message) {

                    console.log(
                        "VU",
                        __VU,
                        message
                    );

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