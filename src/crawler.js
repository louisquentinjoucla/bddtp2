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

// On construit un set contenant tout les monstres
monsters = new Set()

// Crawl monstre/spells
build_db(URLS)


/**
 * Crawl les monstres de http://legacy.aonprd.com/.
 * Afin d'obtenir la liste des monstres -> spells.
 * Stock cette liste au format JSON.
 * @param {Array} URLS Liens des bestiaires que l'ont veut crawler.
 */
async function build_db(URLS){
    for(let i=0; i< URLS.length; i++){
        await get_monsters_url(URLS[i]).then(data => push_bestiaire(data));
    }
    console.log('Writing into monster.json...')
    fs.writeFileSync("../JSON/monster.json", JSON.stringify([...monsters]))
    console.log('Done')
}

/**
 * Pour chacun des monstres contenu dans data,
 * on crée un objet contenant son nom et sa liste des spells
 * @param {Array} data URLS des monstres à crawler
 */
async function push_bestiaire(data) {
    for(let i=0; i<data.length; i++){
        let f = await scrap_monster(data[i]);
        console.log(f);
        monsters.add(f);
    }
}

/**
 * Extrait à partir d'une URL,
 * le nom du monstre ainsi que sa liste de spells.
 * @param {String} url Url du monstre à crawler
 */
async function scrap_monster(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let name = hsv_to_text(url.split('#')[1]);
            let spells= new Set()
            $('[id="'+url.split('#')[1]+'"]').nextUntil('h1').find('a').each((i,elem) =>{
                if(/\.\.\/coreRulebook\/spells\//.test(elem.attribs.href)){
                    let spellname = elem.attribs.href.split('#')[1]
                    spellname = spellname === undefined ? camel_case_to_text(elem.attribs.href.split('/')[elem.attribs.href.split('/').length-1].split('.')[0]): 
                    hsv_to_text(spellname);
                    spells.add(spellname);
                }
            });
            resolve({name: name, spells:[...spells]})
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monster from " + url);
        });
    }).catch(e => console.error(e));
}

/**
 * Extrait à partir d'une URL d'un bestiaire,
 * les urls des monstres à crawler.
 * @param {String} url url contenant une liste d'urls de monstres 
 */
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

/**
 * Transforme un texte en camel case, en texte normal lowercase.
 * @param {String} str Texte en camel case.
 */
function camel_case_to_text(str){
    return str.replace( /([A-Z])/g, " $1" ).trim()
}

/**
 * Transforme un texte au format hsv, en texte normal lowercase.
 * @param {String} str Texte au format hsv.
 */
function hsv_to_text(str){
    return str.toLowerCase().replace(/-/g, ' ').trim()
}