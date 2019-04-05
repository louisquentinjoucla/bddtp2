(function () {

    //
    let components = ["Verbal", "Somatic", "Material"]
    let classes = ["Sorcerer", "Wizard", "Cleric", "Druid", "Ranger", "Bard", "Paladin", "Alchemist", "Summoner", "Witch", "Inquisitor", "Oracle", "Antipaladin", "Magus", "Adept", "Bloodrager", "Shaman"]
    let schools = ["Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation"]

    //Data
    const data = {
        query:{
            name:"",
            advanced:false,
            components:[],
            classes:[],
            schools:[],
            misc:""
        },
        messages:[],
        connected:false,
        components, classes, schools
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
            send(message) { ws.send(JSON.stringify(message)) },
            q(type, element) { data.query[type].includes(element) ? data.query[type].splice(data.query[type].indexOf(element), 1) : data.query[type].push(element) },
            submit() { this.send("result");  console.log(data.query) }
        }
    })

})()



