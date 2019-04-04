(function () {

    //Data
    const data = {
        query:{},
        messages:[],
        connected:false
    }

    //Websocket
    const ws = new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
    ws.onmessage = message => data.messages.push(message.data)
    ws.onopen = () => data.connected = true

    //View
    const view = new Vue({
        el:"#app",
        data,
        methods:{
            send(message) { ws.send(JSON.stringify(message)) }
        }
    })

})()



