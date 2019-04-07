(function () {

    //
    let components = ["Verbal", "Somatic", "Material", "Focus", "Divine Focus"]
    let classes = ["Sorcerer", "Wizard", "Cleric", "Druid", "Ranger", "Bard", "Paladin", "Alchemist", "Summoner", "Witch", "Inquisitor", "Oracle", "Antipaladin", "Magus", "Adept", "Bloodrager", "Shaman"]
    let schools = ["Abjuration", "Conjuration", "Divination", "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation"]

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
        components, classes, schools
    }

    //Websocket
    const ws = new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
    ws.onmessage = message => data.response = JSON.parse(message.data)

    ws.onopen = () => data.connected = true
    ws.onclose = () => data.connected = false

    //View
    const view = new Vue({
        el:"#app",
        data,
        methods:{
            send(message) { ws.send(JSON.stringify(message)) },
            q(type, element) { data.query[type].includes(element) ? data.query[type].splice(data.query[type].indexOf(element), 1) : data.query[type].push(element) },
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



