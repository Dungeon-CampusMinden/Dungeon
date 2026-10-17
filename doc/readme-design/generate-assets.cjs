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
  {id:'soul', file:'soulweaver', number:'01', name:['SOULWEAVER'], color:'#c6a1f0', category:'FANTASY / PROGRAMMING', hook:['BRING A GOLEM','BACK TO LIFE.']},
  {id:'system', file:'system-recovery', number:'02', name:['SYSTEM','RECOVERY'], color:'#78d8dc', category:'SCI-FI / ARRAYS & ALGORITHMS', hook:['REPAIR THE SYSTEM.','QUESTION THE AI.']},
  {id:'last', file:'the-last-hour', number:'03', name:['THE LAST HOUR'], color:'#f3a08d', category:'MYSTERY / CYBERSECURITY', hook:['A MISSING SCIENTIST.','A TICKING CLOCK.']}
];
const esc = s => s.replaceAll('&','&amp;').replaceAll('<','&lt;');
const text = (x,y,size,label,fill=C.white,extra='') => `<text x="${x}" y="${y}" fill="${fill}" font-family="Arial, Helvetica, sans-serif" font-size="${size}" ${extra}>${esc(label)}</text>`;
const caps = (x,y,label,fill=C.gold,size=17) => text(x,y,size,label,fill,'font-weight="700" letter-spacing="3"');
const svg = (w,h,content,defs='') => `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}"><defs>${defs}</defs>${content}</svg>`;
const data = async (file, mime='image/jpeg') => `data:${mime};base64,${(await fs.readFile(file)).toString('base64')}`;
const picture = (uri,x,y,w,h,extra='') => `<image href="${uri}" x="${x}" y="${y}" width="${w}" height="${h}" preserveAspectRatio="xMidYMid slice" ${extra}/>`;
const jpg = async (name,content) => sharp(Buffer.from(content)).jpeg({quality:92,chromaSubsampling:'4:4:4'}).toFile(path.join(output,name));
const vector = async (name,w,h,content,defs='') => fs.writeFile(path.join(output,name),svg(w,h,content,defs));

(async()=>{
 await fs.mkdir(output,{recursive:true});
 for(const game of games)game.image=await data(path.join(repo,'doc/img/readme',game.file+'.jpg'));
 const cat=await data(path.join(repo,'doc/img/logo/cat_logo_128x128.png'),'image/png');
 const heroDefs=`<linearGradient id="fade"><stop stop-color="${C.bg}"/><stop offset=".6" stop-color="${C.bg}" stop-opacity=".3"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></linearGradient><linearGradient id="bottom" x2="0" y2="1"><stop offset=".4" stop-color="${C.bg}" stop-opacity="0"/><stop offset="1" stop-color="${C.bg}"/></linearGradient><radialGradient id="light"><stop stop-color="#775c30" stop-opacity=".18"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></radialGradient>`;
 let hero=`<rect width="1440" height="660" fill="${C.bg}"/><ellipse cx="300" cy="420" rx="650" ry="450" fill="url(#light)"/>`;
 for(let i=0;i<3;i++){
  const x=665+i*263;
  hero+=picture(games[i].image,x,0,300,660).replace('xMidYMid slice','xMaxYMid slice')+`<rect x="${x}" width="300" height="660" fill="${C.bg}" opacity=".22"/><path d="M${x} 0V660" stroke="${C.bg}" stroke-width="9"/>`;
 }
 hero+=`<rect x="560" width="880" height="660" fill="url(#fade)"/><rect width="1440" height="660" fill="url(#bottom)"/>`;
 hero+=picture(cat,53,39,44,44)+caps(111,70,'DUNGEON',C.white,25)+caps(56,134,'DIGITAL EDUCATIONAL ESCAPE ROOMS',C.gold,15);
 hero+=text(48,267,112,'THINK.',C.white,'font-weight="900" letter-spacing="-4"')+text(48,380,112,'SOLVE.',C.white,'font-weight="900" letter-spacing="-4"')+text(48,493,112,'ESCAPE.',C.gold,'font-weight="900" letter-spacing="-4"');
 hero+=text(57,546,23,'Get lost in a story. Find your way through learning.',C.white)+`<path d="M56 583H1384" stroke="${C.line}"/>`;
 hero+=caps(56,623,'THREE ROOMS. THREE WORLDS. YOUR NEXT CHALLENGE.',C.muted,14)+text(1104,623,13,'AI promotional artwork',C.muted);
 await jpg('dungeon-hero.jpg',svg(1440,660,hero,heroDefs));
 let mobile=`<rect width="760" height="940" fill="${C.bg}"/>`;
 games.forEach((g,i)=>{mobile+=picture(g.image,i*254,550,254,390).replace('xMidYMid slice','xMaxYMid slice');});
 mobile+=`<rect y="550" width="760" height="390" fill="url(#bottom)"/>`+picture(cat,40,34,42,42)+caps(99,65,'DUNGEON',C.white,24)+caps(42,121,'EDUCATIONAL ESCAPE ROOMS',C.gold,17);
 mobile+=text(34,251,112,'THINK.',C.white,'font-weight="900" letter-spacing="-4"')+text(34,367,112,'SOLVE.',C.white,'font-weight="900" letter-spacing="-4"')+text(34,483,112,'ESCAPE.',C.gold,'font-weight="900" letter-spacing="-4"');
 mobile+=text(42,536,24,'Three worlds. Your next challenge.',C.white)+text(42,904,19,'AI promotional artwork',C.white);
 await jpg('dungeon-hero-mobile.jpg',svg(760,940,mobile,heroDefs));

 for(const g of games){
  const defs=`<linearGradient id="blend"><stop offset="0" stop-color="${C.bg}"/><stop offset=".38" stop-color="${C.bg}"/><stop offset=".75" stop-color="${C.bg}" stop-opacity=".94"/><stop offset="1" stop-color="${C.bg}" stop-opacity="0"/></linearGradient><linearGradient id="edge" x2="0" y2="1"><stop stop-color="${g.color}" stop-opacity=".22"/><stop offset="1" stop-color="${g.color}" stop-opacity="0"/></linearGradient>`;
  let panel=`<rect width="1440" height="460" fill="${C.bg}"/>`+picture(g.image,580,0,860,460)+`<rect width="1100" height="460" fill="url(#blend)"/><rect width="6" height="460" fill="${g.color}"/><rect x="6" width="570" height="460" fill="url(#edge)"/>`;
  panel+=caps(49,57,`ROOM ${g.number} / ${g.category}`,g.color,15);
  const size=g.name[0]==='SOULWEAVER'?73:g.name.length>1?74:65;
  g.name.forEach((line,i)=>panel+=text(44,166+i*78,size,line,C.white,'font-weight="900" letter-spacing="-2"'));
  const y=g.name.length>1?308:249;
  g.hook.forEach((line,i)=>panel+=text(49,y+i*35,25,line,C.white,'font-weight="700" letter-spacing="1"'));
  panel+=`<path d="M49 378H119" stroke="${g.color}" stroke-width="3"/>`+caps(49,416,'EXPLORE. EXPERIMENT. FIND YOUR WAY OUT.',C.muted,12)+text(1198,438,13,'AI promotional artwork',C.white);
  await jpg('feature-'+g.id+'.jpg',svg(1440,460,panel,defs));
  const down=`<path d="M33 18v23m-8-8 8 8 8-8M23 46h20" fill="none" stroke="${C.bg}" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round"/>`;
  await vector('download-'+g.id+'.svg',280,64,`<rect x="1" y="5" width="278" height="58" rx="5" fill="#03060b"/><rect x="1" y="1" width="278" height="58" rx="5" fill="url(#button)" stroke="${g.color}"/><path d="M7 2H273" stroke="#fff" stroke-opacity=".5"/>${down}`+text(61,39,20,'DOWNLOAD',C.bg,'font-weight="800" letter-spacing="1"')+text(241,37,14,'↗',C.bg),`<linearGradient id="button" x2="0" y2="1"><stop stop-color="${g.color}"/><stop offset="1" stop-color="${g.color}" stop-opacity=".83"/></linearGradient>`);
  await vector('explore-'+g.id+'.svg',280,64,`<rect x="1" y="1" width="278" height="58" rx="5" fill="${C.panel}" stroke="${C.line}"/>`+text(24,38,18,'EXPLORE THE ROOM',C.white,'font-weight="700"')+text(245,39,24,'→',g.color));
 }
 await vector('platforms.svg',1440,96,`<rect width="1440" height="96" fill="${C.panel}"/><path d="M0 1H1440M0 95H1440" stroke="${C.line}"/>`+caps(36,40,'PLAY ON DESKTOP',C.gold,13)+text(36,70,21,'Windows · macOS · Linux',C.white,'font-weight="700"')+`<path d="M488 24V72M974 24V72" stroke="${C.line}"/>`+caps(523,40,'YOUR RUNTIME',C.gold,13)+text(523,70,21,'Java 25 required',C.white,'font-weight="700"')+caps(1008,40,'GAME LANGUAGE',C.gold,13)+text(1008,70,21,'German',C.white,'font-weight="700"'));
 await vector('platforms-mobile.svg',720,252,`<rect width="720" height="252" fill="${C.panel}"/>`+caps(28,36,'PLAY ON DESKTOP',C.gold,16)+text(28,73,30,'Windows · macOS · Linux',C.white,'font-weight="700"')+`<path d="M28 101H692" stroke="${C.line}"/>`+caps(28,140,'YOUR RUNTIME',C.gold,16)+text(28,180,28,'Java 25 required',C.white,'font-weight="700"')+caps(418,140,'GAME LANGUAGE',C.gold,16)+text(418,180,28,'German',C.white,'font-weight="700"'));
 for(const [id,num,label,sub] of [['play','01','PLAY A ROOM','Choose your next challenge'],['create','02','CREATE A ROOM','Meet the DEER-Wizard'],['research','03','THE RESEARCH','Explore the work behind it']]){
  await vector('nav-'+id+'.svg',450,122,`<rect x="1" y="1" width="448" height="120" rx="4" fill="${C.panel}" stroke="${C.line}"/>`+caps(20,30,num,C.gold,14)+text(20,65,25,label,C.white,'font-weight="800"')+text(20,96,17,sub,C.muted)+text(401,68,28,'↗',C.gold));
 }
 const points=[['01','EXPLORE','Read the room. Connect the clues.'],['02','EXPERIMENT','Try an idea. See what changes.'],['03','UNDERSTAND','Turn a solution into a new insight.']];
 let learning=`<rect width="1440" height="248" fill="${C.bg}"/>`;
 points.forEach(([n,h,d],i)=>{let x=42+i*480;learning+=caps(x,52,n,C.gold,16)+text(x,114,35,h,C.white,'font-weight="800"')+text(x,163,20,d,C.muted)+`<path d="M${x} 207H${x+394}" stroke="${C.line}"/>`;});
 await vector('learning-loop.svg',1440,248,learning);
 let flow=`<rect width="1440" height="340" fill="${C.bg}"/>`+caps(46,49,'THE DEER-WIZARD / AUTHORING WORKFLOW',C.gold,15)+text(44,108,40,'YOUR IDEA. YOUR STORY. YOUR ESCAPE ROOM.',C.white,'font-weight="800" letter-spacing="-1"');
 const steps=[['01','LEARNING GOALS','Who learns what?'],['02','STORY & PUZZLES','Build the challenge.'],['03','PLAYABLE ROOM','Package and share.']];
 steps.forEach(([n,h,d],i)=>{let x=46+i*468;flow+=`<rect x="${x}" y="151" width="410" height="139" rx="5" fill="${C.panel}" stroke="${C.line}"/>`+caps(x+21,183,n,C.gold,13)+text(x+21,221,23,h,C.white,'font-weight="700"')+text(x+21,259,19,d,C.muted);if(i<2)flow+=text(x+425,228,25,'→',C.gold);});
 await vector('wizard-workflow.svg',1440,340,flow);
 console.log('Generated 18 README graphics in '+output);
})().catch(e=>{console.error(e);process.exitCode=1;});
