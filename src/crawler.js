const cheerio = require('cheerio');
const request = require('request-promise')
const fs = require('fs')


// Pour chacune des bestiaires on récupère les urls des monstres

URL_BESTIAIRE_1 = 'http://legacy.aonprd.com/bestiary/monsterIndex.html'
URL_BESTIAIRE_2 = 'http://legacy.aonprd.com/bestiary2/additionalMonsterIndex.html'

get_monsters_url(URL_BESTIAIRE_1).then(data => build_db(data));


async function build_db(data) {
    for(let i=0; i<data.length; i++){
        let f = await scrap_monster(data[i]);
        console.log(f);
    }
}
async function scrap_monster(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let name = url.split('#')[1];
            let spells=[]
            $('#'+url.split('#')[1]).nextUntil('h1').find('a').each((i,elem) =>{
                if(/\.\.\/coreRulebook\/spells\//.test(elem.attribs.href)){
                    spells.push(elem.firstChild.data);
                }
            });
            resolve({name: name, spells:spells})
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monster from " + url);
        });
    });
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