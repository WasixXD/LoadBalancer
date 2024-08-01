const workerpool = require('workerpool')

const URL = "http://localhost:8888"
const TRY_REQUESTS = 100;
const LONG_MAX = 9223372036854775
const LUCK_NUMBER = "9223372036854775783"
const WORKER_PATH = "/worker.js"

const pool = workerpool.pool(__dirname + WORKER_PATH, {
    maxQueueSize: TRY_REQUESTS,
    maxWorkers: 9,
})

let tasks = []

for (let i = 0; i < TRY_REQUESTS; i++) {

    const workerData = {
        url: URL + "/prime",
        //number: Math.floor(Math.random() * LONG_MAX)
        number: "9223372036854775783"
    }
    tasks.push(pool.exec('fetchData', [workerData])
        .then(result => console.log(result))
        .catch(err => console.error(err)))

}

Promise.all(tasks).then(() => pool.terminate())
