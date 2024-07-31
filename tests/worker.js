const axios = require('axios');
const workerpool = require("workerpool")
async function fetchData({ url, number }) {
    try {
        const result = await axios.get(`${url}/${number}`, { insecureHTTPParser: true })
        return `recebeu: ${result.data}`
    } catch(e) {
        return `Não foi possivel por ${e} e número ${number}`
    }
 
}

workerpool.worker({
    fetchData
})