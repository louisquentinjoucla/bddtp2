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
            misc:"",
            limit:10
        },
        delay:null,
        response:null,
        connected:false,
        init:false,
        components:[], classes:[], schools:[], levels:[]
    }

    let format = data => (data.charAt(0).toLocaleUpperCase() + data.substr(1)).replace(/ \bs\b/g, "'s")

    //Websocket
    let ws = null
    function reconnect() {
        ws =  new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
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
                parsed.results.forEach(result => {
                    result.monsters = JSON.parse(result.monsters||"[]")
                    result.monsters.forEach(m => m.spells = m.spells.substr(1, m.spells.length-2).split(",").map(s => format(s)).join(", "))
                    result.components = result.components.substr(1, result.components.length-2).split(",").join(", ")
                })
                data.response = parsed
            }
        }

        ws.onopen = () => {
            data.connected = true
            reconnect.attempts = 0
            ws.send(JSON.stringify({init:true}))
        }
        ws.onclose = () => {
            console.log("disconnected")
            data.connected = false
            if (reconnect.attempts++ < 5) reconnect()
        }
    }
    reconnect.attempts = 0
    reconnect()


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
            submit() {
                clearTimeout(data.delay)
                let query = {...data.query, name:data.query.name.replace(/[,']/g, " ")}
                this.send(query)
            },
            format(type, data) {
                switch (type) {
                    case "text": return format(data)
                    case "spell.name": return format(data)
                    case "spell.components": return data
                        .replace(/\bV\b/g, "Verbal")
                        .replace(/\bS\b/g, "Somatic")
                        .replace(/\bM\b/g, "Material")
                        .replace(/\bF\b/g, "Focus")
                        .replace(/\bDF\b/g, "Divine Focus")
                        .replace(/\bE\b/g, "Emotional")
                        .replace(/\bT\b/g, "Thought")
                        .replace(/_/g, "/")
                    default: return data
                }
            },
            delayed_submit() {
                clearTimeout(data.delay)
                data.delay = setTimeout(() => this.submit(), 100)
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

