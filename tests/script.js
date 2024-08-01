import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    vus: 5000,
    duration: '30s',

};

// The function that defines VU logic.
//
// See https://grafana.com/docs/k6/latest/examples/get-started-with-k6/ to learn more
// about authoring k6 scripts.
//
export default function() {
    let response = http.get("http://localhost:8888/prime/" + randomIntBetween(1, 2147483647), { timeout: '10s' })


    check(response, {
        'is status 200': (r) => r.status === 200
    })

    sleep(1)
}
