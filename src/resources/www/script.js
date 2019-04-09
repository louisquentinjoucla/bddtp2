(function () {

    //Data
    const data = {
        query:{
            name:"",
            advanced:false,
            levels:[],
            components:[],
            classes:[],
            schools:[],
            misc:""
        },
        response:null,
        connected:false,
        init:false,
        components:[], classes:[], schools:[], levels:[]
    }

    //Websocket
    const ws = new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
    ws.onmessage = message => {
        let parsed = JSON.parse(message.data)
        //Check if init
        let first = parsed.results[0]||{}
        if ((first.type||[])[0] === "init") {
            delete first.type
            for (let filters in first)
                data[filters] = first[filters]
            data.init = true
        } 
        //Else accept response
        else {
            parsed.results = parsed.results.map(result => JSON.parse(result))
            data.response = parsed
        }
    }

    ws.onopen = () => {
        data.connected = true
        ws.send(JSON.stringify({init:true}))
    }
    ws.onclose = () => data.connected = false

    //View
    const view = new Vue({
        el:"#app",
        data,
        methods:{
            send(message) { ws.send(JSON.stringify(message)) },
            q(type, element) {
                data.query[type].includes(element) ? data.query[type].splice(data.query[type].indexOf(element), 1) : data.query[type].push(element)
                this.submit()
            },
            submit() { this.send(data.query) },
            format(type, data) {
                switch (type) {
                    case "spell.name": return data
                        .charAt(0).toLocaleUpperCase() + data.substr(1)
                    case "spell.components": return data
                        .replace(/\bV\b/g, "Verbal")
                        .replace(/\bS\b/g, "Somatic")
                        .replace(/\bM\b/g, "Material")
                        .replace(/\bF\b/g, "Focus")
                        .replace(/\bDF\b/g, "Divine Focus")
                    default: return data
                }
            }
        },
        computed:{
            results() { return (this.$data.response||{results:[]}).results }
        },
        mounted() {
            document.querySelector("input[name='query']").focus()
        }
    })

})()

