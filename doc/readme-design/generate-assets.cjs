/* README artwork compositor. Requires Node.js and sharp; no game build changes. */
const fs = require('node:fs/promises');
const path = require('node:path');
const sharp = require('sharp');
const args = process.argv.slice(2);
const option = (name, fallback) => args.includes(name) ? args[args.indexOf(name) + 1] : fallback;
const repo = path.resolve(option('--source', path.join(__dirname, '../..')));
const output = path.resolve(option('--output', path.join(repo, 'doc/img/readme')));
const C = {bg:'#0b1018', panel:'#121a26', white:'#f5f1e8', muted:'#a4adba', gold:'#e1bd7b', line:'#303b4c'};
const games = [
  {id:'soul', file:'soulweaver', genre:'FANTASY', name:['SOULWEAVER'], color:'#c6a1f0', category:'FANTASY / PROGRAMMING', hook:['BRING A GOLEM','BACK TO LIFE.']},
  {id:'system', file:'system-recovery', genre:'SCI-FI', name:['SYSTEM','RECOVERY'], color:'#78d8dc', category:'SCI-FI / ARRAYS & ALGORITHMS', hook:['REPAIR THE SYSTEM.','QUESTION THE AI.']},
  {id:'last', file:'the-last-hour', genre:'MYSTERY', name:['THE LAST HOUR'], color:'#f3a08d', category:'MYSTERY / CYBERSECURITY', hook:['A MISSING SCIENTIST.','A TICKING CLOCK.']}
];
const esc = s => s.replaceAll('&','&amp;').replaceAll('<','&lt;');
const text = (x,y,size,label,fill=C.white,extra='') => `<text x="${x}" y="${y}" fill="${fill}" font-family="Arial, Helvetica, sans-serif" font-size="${size}" ${extra}>${esc(label)}</text>`;
const caps = (x,y,label,fill=C.gold,size=17) => text(x,y,size,label,fill,'font-weight="700" letter-spacing="3"');
const svg = (w,h,content,defs='') => `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}"><defs>${defs}</defs>${content}</svg>`;
const data = async (file, mime='image/jpeg') => `data:${mime};base64,${(await fs.readFile(file)).toString('base64')}`;
const picture = (uri,x,y,w,h,extra='') => `<image href="${uri}" x="${x}" y="${y}" width="${w}" height="${h}" preserveAspectRatio="xMidYMid slice" ${extra}/>`;
const jpg = async (name,content) => sharp(Buffer.from(content)).jpeg({quality:92,chromaSubsampling:'4:4:4'}).toFile(path.join(output,name));

(async()=>{
 await fs.mkdir(output,{recursive:true});
 for(const game of games)game.image=await data(path.join(repo,'doc/img/readme',game.file+'.jpg'));
 const cat=await data(path.join(repo,'doc/img/logo/cat_logo_128x128.png'),'image/png');
 const heroDefs=`<linearGradient id="fade"><stop stop-color="${C.bg}"/><stop offset=".6" stop-color="${C.bg}" stop-opacity=".3"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></linearGradient><linearGradient id="bottom" x2="0" y2="1"><stop offset=".4" stop-color="${C.bg}" stop-opacity="0"/><stop offset="1" stop-color="${C.bg}"/></linearGradient><radialGradient id="light"><stop stop-color="#775c30" stop-opacity=".18"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></radialGradient>`;
 let hero=`<rect width="1440" height="500" fill="${C.bg}"/><ellipse cx="300" cy="350" rx="650" ry="450" fill="url(#light)"/>`;
 for(let i=0;i<games.length;i++){
  const x=665+i*(775/games.length);
  hero+=picture(games[i].image,x,0,775/games.length+10,500).replace('xMidYMid slice','xMaxYMid slice')+`<rect x="${x}" width="${775/games.length+10}" height="500" fill="${C.bg}" opacity=".22"/><path d="M${x} 0V500" stroke="${C.bg}" stroke-width="9"/>`;
 }
 hero+=`<rect x="560" width="880" height="500" fill="url(#fade)"/><rect width="1440" height="500" fill="url(#bottom)"/>`;
 hero+=picture(cat,53,39,52,52)+caps(125,72,'DIGITAL EDUCATIONAL ESCAPE ROOMS',C.gold,15);
 hero+=text(48,225,100,'DUNGEON',C.white,'font-weight="900" letter-spacing="-4"')+text(53,290,38,'THINK. SOLVE. ESCAPE.',C.gold,'font-weight="800" letter-spacing="1"');
 hero+=text(56,347,23,'New worlds to explore. New ways to learn.',C.white)+`<path d="M56 413H1384" stroke="${C.line}"/>`;
 hero+=caps(56,458,'PLAY A ROOM. BRING IT TO CLASS. CREATE YOUR OWN.',C.muted,14)+text(1104,458,13,'Image generated with AI',C.muted);
 await jpg('dungeon-hero.jpg',svg(1440,500,hero,heroDefs));
 let mobile=`<rect width="760" height="620" fill="${C.bg}"/>`;
 games.forEach((g,i)=>{mobile+=picture(g.image,i*(760/games.length),300,760/games.length,320).replace('xMidYMid slice','xMaxYMid slice');});
 mobile+=`<rect y="300" width="760" height="320" fill="url(#bottom)"/>`+picture(cat,36,27,50,50)+caps(104,61,'EDUCATIONAL ESCAPE ROOMS',C.gold,19);
 mobile+=text(30,189,101,'DUNGEON',C.white,'font-weight="900" letter-spacing="-4"')+text(38,258,38,'THINK. SOLVE. ESCAPE.',C.gold,'font-weight="800"');
 mobile+=text(36,591,18,'Image generated with AI',C.white);
 await jpg('dungeon-hero-mobile.jpg',svg(760,620,mobile,heroDefs));

 for(const g of games){
  const defs=`<linearGradient id="blend"><stop offset="0" stop-color="${C.bg}"/><stop offset=".38" stop-color="${C.bg}"/><stop offset=".75" stop-color="${C.bg}" stop-opacity=".94"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></linearGradient><linearGradient id="edge" x2="0" y2="1"><stop stop-color="${g.color}" stop-opacity=".22"/><stop offset="1" stop-color="${g.color}" stop-opacity="0"/></linearGradient>`;
  const focusedArt=`<svg x="580" y="0" width="860" height="460" viewBox="500 0 460 540" preserveAspectRatio="${g.id==='last'?'xMaxYMax':'xMaxYMin'} slice">${picture(g.image,0,0,960,540)}</svg>`;
  let panel=`<rect width="1440" height="460" fill="${C.bg}"/>`+focusedArt+`<rect width="1100" height="460" fill="url(#blend)"/><rect width="6" height="460" fill="${g.color}"/><rect x="6" width="570" height="460" fill="url(#edge)"/>`;
  panel+=caps(49,57,g.category,g.color,15);
  const size=g.name[0]==='SOULWEAVER'?73:g.name.length>1?74:65;
  g.name.forEach((line,i)=>panel+=text(44,166+i*78,size,line,C.white,'font-weight="900" letter-spacing="-2"'));
  const y=g.name.length>1?308:249;
  g.hook.forEach((line,i)=>panel+=text(49,y+i*35,25,line,C.white,'font-weight="700" letter-spacing="1"'));
  panel+=`<path d="M49 378H119" stroke="${g.color}" stroke-width="3"/>`+caps(49,416,'EXPLORE. EXPERIMENT. FIND YOUR WAY OUT.',C.muted,12)+`<rect x="1148" y="416" width="270" height="30" rx="3" fill="${C.bg}" opacity=".85"/>`+text(1162,438,13,'Image generated with AI',C.white);
  await jpg('feature-'+g.id+'.jpg',svg(1440,460,panel,defs));
  const mobileNames=g.id==='soul'?['SOUL','WEAVER']:g.id==='last'?['THE LAST','HOUR']:g.name;
  let compact=`<rect width="760" height="430" fill="${C.bg}"/>`+picture(g.image,315,0,445,430).replace('xMidYMid slice','xMaxYMid slice')+`<rect width="590" height="430" fill="url(#blend)"/><rect width="5" height="430" fill="${g.color}"/>`;
  compact+=caps(28,43,g.genre,g.color,20);
  mobileNames.forEach((line,i)=>compact+=text(24,145+i*67,g.id==='system'?54:60,line,C.white,'font-weight="900" letter-spacing="-1"'));
  g.hook.forEach((line,i)=>compact+=text(28,283+i*32,21,line,C.white,'font-weight="700"'));
  compact+=text(28,405,17,'Image generated with AI',C.muted);
  await jpg('feature-'+g.id+'-mobile.jpg',svg(760,430,compact,defs));
 }
 console.log('Generated README artwork in '+output);
})().catch(e=>{console.error(e);process.exitCode=1;});
