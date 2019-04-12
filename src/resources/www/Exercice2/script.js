(function () {

    //Websocket
    let ws = null
    function reconnect() {
        ws =  new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
        ws.onmessage = message => {let parsed = JSON.parse(message.data)}
        ws.onopen = () => {}
        ws.onclose = () => {
            if (reconnect.attempts++ < 5) reconnect()
        }
    }
    reconnect.attempts = 0
    reconnect()


})()

