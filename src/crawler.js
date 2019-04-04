const cheerio = require('cheerio');
const request = require('request-promise')
const fs = require('fs')


// Pour chacune des bestiaires on récupère les urls des monstres

URL_BESTIAIRE_1 = 'http://legacy.aonprd.com/bestiary/monsterIndex.html'
URL_BESTIAIRE_2 = 'http://legacy.aonprd.com/bestiary2/additionalMonsterIndex.html'
URL_BESTIAIRE_3 = 'http://legacy.aonprd.com/bestiary3/monsterIndex.html'
URL_BESTIAIRE_4 = 'http://legacy.aonprd.com/bestiary4/monsterIndex.html'
URL_BESTIAIRE_5 = 'http://legacy.aonprd.com/bestiary5/index.html'

URLS = [URL_BESTIAIRE_1, URL_BESTIAIRE_2, URL_BESTIAIRE_3, URL_BESTIAIRE_4, URL_BESTIAIRE_5]
monsters = new Set()
build_db(URLS)


async function build_db(URLS){
    for(let i=0; i< URLS.length; i++){
        await get_monsters_url(URLS[i]).then(data => push_bestiaire(data));
    }
    console.log('Writing into monster.json...')
    fs.writeFileSync("../JSON/monster.json", JSON.stringify([...monsters]))
    console.log('Done')
}

async function push_bestiaire(data) {
    for(let i=0; i<data.length; i++){
        let f = await scrap_monster(data[i]);
        console.log(f);
        monsters.add(f);
    }
}

async function scrap_monster(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let name = url.split('#')[1];
            let spells= new Set()
            $('[id="'+url.split('#')[1]+'"]').nextUntil('h1').find('a').each((i,elem) =>{
                if(/\.\.\/coreRulebook\/spells\//.test(elem.attribs.href)){
                    let spellname = elem.attribs.href.split('#')[1]
                    spellname = spellname === undefined ? elem.attribs.href.split('/')[elem.attribs.href.split('/').length-1].split('.')[0]: spellname;
                    spells.add(spellname);
                }
            });
            resolve({name: name, spells:[...spells]})
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monster from " + url);
        });
    }).catch(e => console.error(e));
}

async function get_monsters_url(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let monsters_url = [];
            monsters_li = $('.body-content .body #monster-index-wrapper li a').each((i,elem)=>{
                if(/\.html\#/.test(elem.attribs.href)){monsters_url.push(url.substring(0,url.lastIndexOf('/')+1)+ elem.attribs.href)}
            });
            resolve(monsters_url);
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monsterlist from " + url);
        });
    });
}