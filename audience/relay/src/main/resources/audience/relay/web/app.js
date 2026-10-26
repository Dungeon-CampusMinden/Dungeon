// Controller page of the Dungeon audience mode, on whatever device each player uses.
//
// The game publishes a public state through the relay; this page streams it and shows only the
// controls of the current game context. Inputs go back as votes for the current window, or as
// survey answers. The page keeps its credentials per session code in localStorage, so a reload
// rejoins as the same participant.
"use strict";

const API = "api/sessions/";
const app = document.getElementById("app");

// Texts of the page. Dialog texts and vote labels come from the game in its own language.
const TEXTS = {
  de: {
    name: "Deutsch",
    language: "Sprache",
    title: "Dungeon gemeinsam spielen",
    intro: "Alle stimmen ab, die Figur macht, was die meisten wählen.",
    codeLabel: "Code vom Spielbildschirm",
    join: "Beitreten",
    unknownCode: "Diesen Code gibt es nicht.",
    roundFull: "Runde voll oder schon vorbei.",
    unreachable: "Server nicht erreichbar.",
    roundOver: "Diese Runde ist vorbei.",
    connecting: "Verbinde …",
    connected: "verbunden",
    offline: "keine Verbindung",
    otherTab: "in einem anderen Tab offen",
    closedTitle: "Die Runde ist vorbei",
    closedLead: "Danke fürs Mitspielen. Ihr könnt die Seite schließen.",
    joinedTitle: "Du bist drin",
    joinedLead: "Gleich erscheint die erste Frage.",
    doneTitle: "Danke!",
    doneLead: "Deine Antworten sind angekommen. Ihr könnt die Seite schließen.",
    spectatorTitle: "Du schaust zu",
    spectatorLead: "Ohne Einwilligung speichern wir nichts von dir. Dafür stimmst du nicht mit ab.",
    soon: "Gleich geht es los",
    lobbyLead: "Schaut auf den Spielbildschirm. Die Spielleitung startet die Runde.",
    endedTitle: "Geschafft",
    watchScreen: "Schaut auf den Spielbildschirm.",
    pausedTitle: "Pausiert",
    pausedLead: "Gleich geht es weiter. Abgegebene Stimmen bleiben erhalten.",
    moveTitle: "Wohin geht es?",
    choiceTitle: "Antwort wählen",
    choicesTitle: "Antworten wählen",
    textTitle: "Antwort eintippen",
    codeTitle: "Code eingeben",
    readTitle: "Text lesen",
    readScreenTitle: "Text auf dem Spielbildschirm lesen",
    watchTitle: "Schaut auf den Spielbildschirm",
    firstVote: "Die erste Stimme startet die Runde.",
    secondsLeft: "Sekunden übrig",
    chosen: (label) => "Gewählt: " + label,
    up: "oben",
    left: "links",
    right: "rechts",
    down: "unten",
    use: "Benutzen",
    space: "Leertaste",
    moveVote: (name) => "Deine Stimme: " + name + ". Du kannst sie bis zum Ende ändern.",
    moveHint: "Wähle eine Richtung oder „Benutzen“.",
    moveKeys: "Auf der Tastatur: Pfeiltasten und Leertaste.",
    answer: (number) => "Antwort " + number,
    choiceVote: (label) => "Deine Stimme: " + label + ". Die Antworten stehen auf dem Spielbildschirm.",
    choiceHint: "Die Antworten stehen auf dem Spielbildschirm.",
    choiceKeys: (count) => "Auf der Tastatur: Zifferntasten 1 bis " + count + ".",
    submit: "Abschicken",
    resubmit: "Neu abschicken",
    submitted: (list) => "Abgeschickt: " + list + ". Ihr stimmt über die ganze Kombination ab.",
    choicesHint: "Wähle alle passenden Antworten, dann abschicken.",
    choicesKeys: "Auf der Tastatur: Zifferntasten wählen, Enter schickt ab.",
    textPlaceholder: "Eure Antwort",
    textLabel: "Antwort",
    send: "Senden",
    change: "Ändern",
    textVote: (text) => "Deine Antwort: „" + text + "“",
    textHint: "Gleiche Antworten werden zusammengezählt.",
    erase: "Löschen",
    codeVote: (digits) => "Deine Stimme: " + digits,
    codeHint: "Führende Nullen zählen mit.",
    codeKeys: "Auf der Tastatur: Ziffern, Rücktaste und Enter.",
    ready: "Bereit",
    next: "Weiter",
    continueHint: "Sobald die Hälfte bereit ist, geht es nach 5 Sekunden weiter.",
    back: "Zurück",
    sending: "Wird gesendet …",
    rejected: "Die Antworten wurden nicht angenommen. Bitte prüft sie und schickt sie erneut.",
    sendFailed: "Senden hat nicht geklappt. Prüft die Verbindung und versucht es noch einmal.",
    page: (page, pages) => "Seite " + page + " von " + pages,
    required: "Bitte beantworten.",
    maxChars: (max) => "Höchstens " + max + " Zeichen.",
    number: "Bitte eine Zahl eingeben.",
    wholeNumber: "Bitte eine ganze Zahl eingeben.",
    min: (min) => "Mindestens " + min + ".",
    max: (max) => "Höchstens " + max + ".",
    otherMissing: "Bitte „Sonstiges“ ausfüllen.",
    everyRow: "Bitte jede Zeile beantworten.",
    pick: "Bitte wählen",
    other: "Sonstiges",
  },
  en: {
    name: "English",
    language: "Language",
    title: "Play Dungeon together",
    intro: "Everyone votes, and the character does what most people pick.",
    codeLabel: "Code from the main screen",
    join: "Join",
    unknownCode: "This code does not exist.",
    roundFull: "Round is full or already over.",
    unreachable: "Cannot reach the server.",
    roundOver: "This round is over.",
    connecting: "Connecting …",
    connected: "connected",
    offline: "no connection",
    otherTab: "open in another tab",
    closedTitle: "The round is over",
    closedLead: "Thanks for playing. You can close this page.",
    joinedTitle: "You're in",
    joinedLead: "The first question will appear shortly.",
    doneTitle: "Thank you!",
    doneLead: "We received your answers. You can close this page.",
    spectatorTitle: "You're watching",
    spectatorLead: "Without your consent, we store nothing about you. You just can't vote.",
    soon: "Starting soon",
    lobbyLead: "Watch the main screen. The host will start the round.",
    endedTitle: "You made it",
    watchScreen: "Watch the main screen.",
    pausedTitle: "Paused",
    pausedLead: "The game continues shortly. Votes already cast still count.",
    moveTitle: "Which way?",
    choiceTitle: "Pick an answer",
    choicesTitle: "Pick answers",
    textTitle: "Type your answer",
    codeTitle: "Enter the code",
    readTitle: "Read the text",
    readScreenTitle: "Read the text on the main screen",
    watchTitle: "Watch the main screen",
    firstVote: "The first vote starts the round.",
    secondsLeft: "seconds left",
    chosen: (label) => "Chosen: " + label,
    up: "up",
    left: "left",
    right: "right",
    down: "down",
    use: "Use",
    space: "Space",
    moveVote: (name) => "Your vote: " + name + ". You can change it until time runs out.",
    moveHint: "Pick a direction or “Use”.",
    moveKeys: "On the keyboard: arrow keys and Space.",
    answer: (number) => "Answer " + number,
    choiceVote: (label) => "Your vote: " + label + ". The answers are on the main screen.",
    choiceHint: "The answers are on the main screen.",
    choiceKeys: (count) => "On the keyboard: number keys 1 to " + count + ".",
    submit: "Submit",
    resubmit: "Submit again",
    submitted: (list) => "Submitted: " + list + ". You vote on the whole combination.",
    choicesHint: "Pick every answer that fits, then submit.",
    choicesKeys: "On the keyboard: pick with number keys, submit with Enter.",
    textPlaceholder: "Your answer",
    textLabel: "Answer",
    send: "Send",
    change: "Change",
    textVote: (text) => "Your answer: “" + text + "”",
    textHint: "Identical answers are counted together.",
    erase: "Delete",
    codeVote: (digits) => "Your vote: " + digits,
    codeHint: "Leading zeros count.",
    codeKeys: "On the keyboard: digits, Backspace and Enter.",
    ready: "Ready",
    next: "Next",
    continueHint: "Once half of you are ready, the game continues after 5 seconds.",
    back: "Back",
    sending: "Sending …",
    rejected: "Your answers were not accepted. Please check them and submit again.",
    sendFailed: "Sending failed. Check your connection and try again.",
    page: (page, pages) => "Page " + page + " of " + pages,
    required: "Please answer this.",
    maxChars: (max) => "At most " + max + " characters.",
    number: "Please enter a number.",
    wholeNumber: "Please enter a whole number.",
    min: (min) => "At least " + min + ".",
    max: (max) => "At most " + max + ".",
    otherMissing: "Please fill in “Other”.",
    everyRow: "Please answer every row.",
    pick: "Please choose",
    other: "Other",
  },
};
const LANGUAGE_COOKIE = "dungeon-audience-language";
let language = initialLanguage();

const DIRECTIONS = [
  { value: "up", label: "▲", area: "up", key: "ArrowUp" },
  { value: "left", label: "◀", area: "left", key: "ArrowLeft" },
  { value: "right", label: "▶", area: "right", key: "ArrowRight" },
  { value: "down", label: "▼", area: "down", key: "ArrowDown" },
];

const session = {
  code: null,
  token: null,
  pid: null,
  state: null,
  stateAt: 0,
  online: false,
  events: null,
  failures: 0,
  replaced: false,
  surveys: {},
};
// Votes, survey drafts, and typed answers belong to one round; forget() clears them.
const votes = new Map();
let drafts = {};
// What the device typed or picked per puzzle, so a new window, a pause, or a language switch keeps
// unsent answers.
const typed = new Map();
let screenKey = "";

// --- helpers

function el(tag, attributes = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attributes)) {
    if (value === undefined || value === null || value === false) continue;
    if (key === "class") node.className = value;
    else if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
    else node.setAttribute(key, value === true ? "" : value);
  }
  for (const child of children.flat()) {
    if (child === null || child === undefined || child === false) continue;
    node.append(child instanceof Node ? child : document.createTextNode(String(child)));
  }
  return node;
}

/** Page text in the current language; texts with values are functions of them. */
function t(key, ...values) {
  const text = TEXTS[language][key];
  return typeof text === "function" ? text(...values) : text;
}

/** The language picked on this device before, otherwise the first one the browser prefers. */
function initialLanguage() {
  const stored = document.cookie.match(new RegExp("(?:^|; )" + LANGUAGE_COOKIE + "=(\\w+)"));
  if (stored && Object.hasOwn(TEXTS, stored[1])) return stored[1];
  const preferred = (navigator.languages || [navigator.language || ""])
    .map((tag) => tag.split("-")[0].toLowerCase())
    .find((code) => Object.hasOwn(TEXTS, code));
  return preferred || "en";
}

/** Switches the page language, remembers it in a cookie, and redraws the screen in place. */
function switchLanguage(code) {
  if (code === language) return;
  language = code;
  document.cookie = LANGUAGE_COOKIE + "=" + code + "; max-age=31536000; SameSite=Lax";
  applyLanguage();
  const input = app.querySelector(".code-input");
  if (input) app.replaceChildren(...joinScreen("", input.value));
  else render(true);
}

function applyLanguage() {
  document.documentElement.lang = language;
  document.title = t("title");
}

/** Text of a survey string in the page language, falling back to the other one. */
function surveyText(text) {
  return text ? (text[language] ?? text.de ?? text.en ?? "") : "";
}

/** Survey text with `code` spans as <code>. */
function localized(text) {
  return surveyText(text).split("`").map((part, i) => (i % 2 === 1 ? el("code", {}, part) : part));
}

function spaced(code) {
  return code && code.length === 6 ? code.slice(0, 3) + " " + code.slice(3) : code || "";
}

function storageKey(code) {
  return "dungeon-audience:" + code;
}

async function post(path, body) {
  const response = await fetch(API + session.code + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    const error = new Error("HTTP " + response.status);
    error.status = response.status;
    throw error;
  }
  return response.json();
}

// --- session

async function join(code, token) {
  session.code = code;
  const joined = await post("/join", token ? { token } : {});
  session.token = joined.token;
  session.pid = joined.pid;
  session.surveys = joined.surveys;
  localStorage.setItem(storageKey(code), JSON.stringify({ token: joined.token, pid: joined.pid }));
  history.replaceState(null, "", "?code=" + code);
  connect();
}

function connect() {
  if (session.events) session.events.close();
  const url = API + session.code + "/events?token=" + encodeURIComponent(session.token);
  const events = new EventSource(url);
  session.events = events;
  events.addEventListener("state", (event) => {
    const message = JSON.parse(event.data);
    session.state = message.state;
    session.stateAt = performance.now() - message.ageMs;
    session.online = true;
    session.failures = 0;
    render();
  });
  // The same device opened the round in another tab; reconnecting here would take the stream back
  // and the two tabs would disconnect each other.
  events.addEventListener("replaced", () => {
    events.close();
    session.online = false;
    session.replaced = true;
    updateBar();
  });
  events.addEventListener("open", () => {
    session.online = true;
    updateBar();
  });
  events.addEventListener("error", async () => {
    session.online = false;
    session.failures++;
    updateBar();
    // EventSource retries network errors on its own, but gives up after an HTTP error, such as a
    // session that is gone or too many open streams. Repeated failures may mean the session is gone.
    const closed = events.readyState === EventSource.CLOSED;
    if (!closed && session.failures !== 3) return;
    try {
      await post("/join", { token: session.token });
    } catch (error) {
      if (error.status === 404 || error.status === 409) {
        events.close();
        forget();
        showJoin(t("roundOver"));
        return;
      }
    }
    if (closed) setTimeout(() => session.events === events && connect(), 3000);
  });
}

/** Leaves a round that is over, so the next code starts with the consent question again. */
function forget() {
  if (session.code) localStorage.removeItem(storageKey(session.code));
  session.code = session.token = session.pid = session.state = null;
  votes.clear();
  typed.clear();
  drafts = {};
}

async function vote(value) {
  const context = session.state.context;
  const token = session.token;
  const before = votes.get(context.window);
  votes.set(context.window, value);
  render(true);
  try {
    await post("/input", { token, type: "vote", window: context.window, value });
  } catch {
    // The round ended while the vote was underway; the next round starts without votes.
    if (session.token !== token) return;
    // The game still counts the vote it accepted before; show that one again.
    if (before === undefined) votes.delete(context.window);
    else votes.set(context.window, before);
    render(true);
  }
}

// --- rendering

/** Rebuilds the screen when what it shows changed; otherwise only the timer updates. */
function render(force = false) {
  const key = currentKey();
  if (force || key !== screenKey) {
    screenKey = key;
    app.replaceChildren(...build().filter(Boolean));
    const focus = app.querySelector("[data-autofocus]");
    if (focus) focus.focus();
  }
  updateBar();
  updateResult();
  updateTimer();
}

function status() {
  const players = session.state && session.state.players;
  return players && players[session.pid];
}

function currentKey() {
  if (!session.code || !session.token) return "join";
  const state = session.state;
  if (!state) return "connecting";
  if (state.phase === "closed") return "closed";
  const own = status() || "joining";
  const context = state.context || {};
  if (own === "play" || own === "spectator") {
    return [own, state.phase, state.paused, context.kind, context.window, context.options, context.multiple,
      context.digits, "windowMs" in context, context.title, context.text].join("|");
  }
  return own + "|" + state.phase;
}

function build() {
  const key = screenKey;
  if (key === "join") return joinScreen();
  if (key === "connecting") return [bar(), statusScreen(t("connecting"))];
  if (key === "closed") return [languageBar(), statusScreen(t("closedTitle"), t("closedLead"))];
  const own = status() || "joining";
  switch (own) {
    case "joining":
      return [bar(), statusScreen(t("joinedTitle"), t("joinedLead"))];
    case "pre":
      return surveyScreen("pre");
    case "post":
      return surveyScreen("post");
    case "done":
      return [bar(), statusScreen(t("doneTitle"), t("doneLead"))];
    case "spectator":
      return spectatorScreen();
    default:
      return playScreen();
  }
}

// A screen without inputs: the message sits in the middle of the free space below the bar.
function statusScreen(title, lead, ...extra) {
  return el("section", { class: "status" },
    el("h1", {}, title), lead ? el("p", { class: "lead" }, lead) : null, ...extra);
}

function bar() {
  return el("div", { class: "bar" },
    el("span", { class: "code", "aria-label": "Code " + session.code }, spaced(session.code)),
    el("span", { class: "end" }, el("span", { "data-online": true }), languageSwitch()));
}

/** The bar of screens outside a round: only the language switch. */
function languageBar() {
  return el("div", { class: "bar" }, el("span"), languageSwitch());
}

function languageSwitch() {
  return el("span", { class: "language", role: "group", "aria-label": t("language") },
    ...Object.keys(TEXTS).map((code) => el("button", {
      type: "button",
      lang: code,
      title: TEXTS[code].name,
      "aria-label": TEXTS[code].name,
      "aria-pressed": String(code === language),
      onclick: () => switchLanguage(code),
    }, code.toUpperCase())));
}

/** Shows the connection state in place, so a short interruption keeps entered answers. */
function updateBar() {
  const status = app.querySelector("[data-online]");
  if (!status) return;
  status.textContent = t(session.replaced ? "otherTab" : session.online ? "connected" : "offline");
  status.classList.toggle("offline", !session.online);
}

function showJoin(error) {
  screenKey = "";
  session.code = null;
  app.replaceChildren(...joinScreen(error));
}

/** The code without spaces or other characters, at most six digits; pasting "123 456" works. */
function digitsOnly(text) {
  return text.replace(/\D/g, "").slice(0, 6);
}

function joinScreen(error, code = new URLSearchParams(location.search).get("code") || "") {
  const input = el("input", {
    class: "code-input",
    type: "text",
    inputmode: "numeric",
    autocomplete: "off",
    placeholder: "000000",
    value: digitsOnly(code),
    "data-autofocus": true,
  });
  // Errors show inside the code box, so nothing below it moves.
  const message = el("p", { class: "error", role: "alert" });
  const box = el("div", { class: "code-box" }, input, message);
  const submit = el("button", { class: "button primary", type: "submit" }, t("join"));
  const showError = (text) => {
    message.textContent = text;
    box.classList.toggle("invalid", Boolean(text));
  };
  const update = () => {
    const digits = digitsOnly(input.value);
    if (input.value !== digits) input.value = digits;
    submit.disabled = digits.length !== 6;
  };
  input.addEventListener("input", () => {
    update();
    showError("");
  });
  update();
  showError(error || "");
  const form = el("form", {
    class: "field join",
    onsubmit: async (event) => {
      event.preventDefault();
      const code = input.value;
      if (code.length !== 6) return;
      submit.disabled = true;
      // Joining the same round again keeps the participant, for example after closing the tab.
      const stored = localStorage.getItem(storageKey(code));
      try {
        await join(code, stored ? JSON.parse(stored).token : null);
      } catch (failure) {
        session.code = null;
        submit.disabled = false;
        showError(t(failure.status === 404 ? "unknownCode" : failure.status === 409 ? "roundFull" : "unreachable"));
      }
    },
  }, el("label", { for: "code" }, t("codeLabel")), box, submit);
  input.id = "code";
  return [
    languageBar(),
    el("h1", {}, t("title")),
    el("p", { class: "lead" }, t("intro")),
    el("div", { class: "controls" }, form),
  ];
}

function spectatorScreen() {
  const state = session.state;
  return [
    bar(),
    statusScreen(t("spectatorTitle"), t("spectatorLead"),
      state.phase === "playing" && !state.paused ? timerRow() : null,
      resultBlock()),
  ];
}

function playScreen() {
  const state = session.state;
  if (state.phase === "lobby") {
    return [bar(), statusScreen(t("soon"), t("lobbyLead"))];
  }
  if (state.phase === "countdown") {
    return [bar(), statusScreen(t("soon"), null, el("div", { class: "big", "data-countdown": true }, ""))];
  }
  if (state.phase === "ended") {
    return [bar(), statusScreen(t("endedTitle"), t("watchScreen"))];
  }
  // The host paused the game: the window and its time stand still, and votes cast so far stay.
  if (state.paused) {
    const screen = statusScreen(t("pausedTitle"), t("pausedLead"));
    screen.prepend(el("div", { class: "sign", "aria-hidden": "true" }, "II"));
    return [bar(), screen];
  }
  const context = state.context || { kind: "wait" };
  const title = t({
    move: "moveTitle",
    choice: context.multiple ? "choicesTitle" : "choiceTitle",
    text: "textTitle",
    code: "codeTitle",
    continue: context.text ? "readTitle" : "readScreenTitle",
  }[context.kind] || "watchTitle");
  const controls = {
    move: movePad,
    choice: choicePad,
    text: textEntry,
    code: codePad,
    continue: continuePad,
  }[context.kind];
  if (!controls) return [bar(), statusScreen(title, null, resultBlock())];
  return [
    bar(),
    el("h1", {}, title),
    promptBlock(context),
    timerRow(),
    resultBlock(),
    el("div", { class: "controls" }, controls(context)),
  ];
}

// Title and question of the dialog on the game screen, so nobody has to read them from afar.
function promptBlock(context) {
  if (!context.title && !context.text) return null;
  return el("section", { class: "prompt" },
    context.title ? el("h2", {}, context.title) : null,
    context.text ? el("p", {}, context.text) : null);
}

function timerRow() {
  const context = session.state.context || {};
  if (!("windowMs" in context)) {
    const text = context.kind === "move" ? t("firstVote") : "";
    return el("p", { class: "note window" }, text);
  }
  const dots = el("div", { class: "dots", "aria-hidden": "true" });
  for (let i = 0; i < 20; i++) dots.append(el("i"));
  return el("div", { class: "window", "data-timer": true },
    dots, el("span", { class: "seconds", "aria-label": t("secondsLeft") }, ""));
}

function updateTimer() {
  const state = session.state;
  if (!state) return;
  const elapsed = performance.now() - session.stateAt;
  const countdown = app.querySelector("[data-countdown]");
  if (countdown) countdown.textContent = Math.max(1, Math.ceil(((state.countdownMs || 0) - elapsed) / 1000));
  const timer = app.querySelector("[data-timer]");
  if (!timer || !state.context) return;
  const remaining = Math.max(0, (state.context.remainingMs || 0) - elapsed);
  const total = Math.max(1, state.context.windowMs || 1);
  const lit = Math.ceil((remaining / total) * 20);
  timer.querySelectorAll("i").forEach((dot, i) => dot.classList.toggle("on", i < lit));
  timer.querySelector(".seconds").textContent = Math.ceil(remaining / 1000);
}

/** Holder of the last window's result; updated in place so entered answers stay. */
function resultBlock() {
  return el("div", { "data-result": true, role: "status" });
}

function updateResult() {
  const holder = app.querySelector("[data-result]");
  if (!holder) return;
  const result = session.state && session.state.result;
  const key = result ? JSON.stringify(result) : "";
  if (holder.dataset.key === key) return;
  holder.dataset.key = key;
  if (!result) {
    holder.className = "";
    holder.replaceChildren();
    return;
  }
  const tally = (result.tally || []).map((entry) => entry.count + " × " + entry.label).join(", ");
  holder.className = "result";
  holder.replaceChildren(...[el("strong", {}, t("chosen", result.chosen)), tally ? el("span", {}, tally) : null].filter(Boolean));
}

function ownVote() {
  return votes.get(session.state.context.window);
}

function movePad() {
  const mine = ownVote();
  const buttons = DIRECTIONS.map((direction) => el("button", {
    class: "button " + direction.area + (mine === direction.value ? " picked" : ""),
    "aria-label": t(direction.value),
    "aria-pressed": String(mine === direction.value),
    "data-key": direction.key,
    onclick: () => vote(direction.value),
  }, direction.label));
  const act = el("button", {
    class: "button primary act" + (mine === "interact" ? " picked" : ""),
    "aria-pressed": String(mine === "interact"),
    "data-key": "Space",
    onclick: () => vote("interact"),
  }, t("use"), el("kbd", {}, t("space")));
  const name = mine ? t(mine === "interact" ? "use" : mine) : null;
  return [
    el("div", { class: "pad" }, ...buttons, act),
    el("p", { class: "note" }, name ? t("moveVote", name) : t("moveHint")),
    keysHint(t("moveKeys")),
  ];
}

function choicePad(context) {
  const count = context.options || 0;
  const labels = context.labels || null;
  const mine = ownVote();
  if (!context.multiple) {
    const grid = el("div", { class: "options" + (labels ? " labels" : "") });
    for (let i = 1; i <= count; i++) {
      grid.append(el("button", {
        class: "button" + (mine !== undefined && mine !== i ? " off" : "") + (mine === i ? " picked" : ""),
        "aria-label": labels ? labels[i - 1] : t("answer", i),
        "aria-pressed": String(mine === i),
        "data-key": String(i),
        onclick: () => vote(i),
      }, labels ? labels[i - 1] : i));
    }
    return [
      grid,
      el("p", { class: "note" }, mine ? t("choiceVote", labels ? labels[mine - 1] : mine) : t("choiceHint")),
      keysHint(t("choiceKeys", count)),
    ];
  }
  const sent = Array.isArray(mine) ? mine : [];
  const picked = new Set(typed.get(typedKey(context)) ?? sent);
  const grid = el("div", { class: "options" });
  const send = el("button", { class: "button primary", "data-key": "Enter" }, t(sent.length ? "resubmit" : "submit"), el("kbd", {}, "Enter"));
  const refresh = () => {
    grid.querySelectorAll("button").forEach((button, i) => {
      const on = picked.has(i + 1);
      button.classList.toggle("off", !on);
      button.setAttribute("aria-pressed", String(on));
    });
    send.disabled = picked.size === 0;
  };
  for (let i = 1; i <= count; i++) {
    grid.append(el("button", {
      class: "button",
      "aria-label": t("answer", i),
      "data-key": String(i),
      onclick: () => {
        picked.has(i) ? picked.delete(i) : picked.add(i);
        typed.set(typedKey(context), [...picked]);
        refresh();
      },
    }, i));
  }
  send.addEventListener("click", () => vote([...picked].sort((a, b) => a - b)));
  refresh();
  if (picked.size === 0) grid.querySelectorAll("button").forEach((b) => b.classList.remove("off"));
  return [
    grid,
    send,
    el("p", { class: "note" }, sent.length ? t("submitted", sent.join(" + ")) : t("choicesHint")),
    keysHint(t("choicesKeys")),
  ];
}

function typedKey(context) {
  return [context.kind, context.title, context.text].join("|");
}

function textEntry(context) {
  const mine = ownVote();
  const input = el("input", {
    type: "text",
    autocomplete: "off",
    autocapitalize: "off",
    spellcheck: "false",
    maxlength: "120",
    placeholder: t("textPlaceholder"),
    "aria-label": t("textLabel"),
    value: typed.get(typedKey(context)) ?? mine ?? "",
  });
  input.addEventListener("input", () => typed.set(typedKey(context), input.value));
  const send = el("button", { class: "button primary", type: "submit" }, t(mine ? "change" : "send"));
  const form = el("form", {
    class: "field",
    onsubmit: (event) => {
      event.preventDefault();
      const value = input.value.trim();
      if (value) vote(value);
    },
  }, input, send);
  return [form, el("p", { class: "note" }, mine ? t("textVote", mine) : t("textHint"))];
}

function codePad(context) {
  const length = context.digits;
  let digits = typed.get(typedKey(context)) ?? ownVote() ?? "";
  const display = el("div", { class: "display", "aria-live": "polite" }, "");
  const send = el("button", { class: "button primary word", "data-key": "Enter" }, "OK");
  const show = () => {
    typed.set(typedKey(context), digits);
    display.textContent = digits.padEnd(length, "_");
    send.disabled = digits.length !== length;
  };
  const keypad = el("div", { class: "keypad" });
  for (const key of ["1", "2", "3", "4", "5", "6", "7", "8", "9"]) {
    keypad.append(el("button", { class: "button", "data-key": key, onclick: () => { if (digits.length < length) digits += key; show(); } }, key));
  }
  keypad.append(el("button", { class: "button word", "aria-label": t("erase"), "data-key": "Backspace", onclick: () => { digits = digits.slice(0, -1); show(); } }, "⌫"));
  keypad.append(el("button", { class: "button", "data-key": "0", onclick: () => { if (digits.length < length) digits += "0"; show(); } }, "0"));
  send.addEventListener("click", () => vote(digits));
  keypad.append(send);
  show();
  const mine = ownVote();
  return [
    display,
    keypad,
    el("p", { class: "note" }, mine ? t("codeVote", mine) : t("codeHint")),
    keysHint(t("codeKeys")),
  ];
}

function continuePad() {
  const mine = ownVote();
  return [
    el("button", { class: "button primary" + (mine ? " picked" : ""), "data-key": "Enter", onclick: () => vote("continue"), disabled: Boolean(mine) }, t(mine ? "ready" : "next"), mine ? null : el("kbd", {}, "Enter")),
    el("p", { class: "note" }, t("continueHint")),
  ];
}

// Keyboard hint that only devices with a mouse show; touch screens have no keys to press.
function keysHint(text) {
  return el("p", { class: "note keys" }, text);
}

// --- keyboard

// On laptops a key presses the control button that carries it in data-key; typing into a field
// or using the language switch keeps its normal meaning.
document.addEventListener("keydown", (event) => {
  if (event.repeat || event.target.closest("input, textarea, select, .language") || event.altKey || event.ctrlKey || event.metaKey) return;
  const key = event.key === " " ? "Space" : event.key;
  const button = app.querySelector(`.controls [data-key="${CSS.escape(key)}"]`);
  if (!button || button.disabled) return;
  event.preventDefault();
  button.click();
});

// --- surveys

// The game registers its surveys with the session; joining hands them to the device.
function surveyScreen(phase) {
  const holder = el("div", { class: "survey" });
  renderSurvey(holder, phase, session.surveys[phase]);
  return [bar(), holder];
}

function renderSurvey(holder, phase, survey) {
  const draft = (drafts[phase] ||= { page: 0, answers: {} });
  const page = survey.pages[draft.page];
  const last = draft.page === survey.pages.length - 1;
  const fields = page.questions.map((question) => questionField(question, draft.answers));
  const message = el("p", { class: "error", role: "alert" });
  const declining = () => declines(survey, draft.answers);
  const next = el("button", { class: "button primary" }, t(last || declining() ? "submit" : "next"));
  const back = draft.page > 0 ? el("button", { class: "button", onclick: () => { draft.page--; renderSurvey(holder, phase, survey); } }, t("back")) : null;
  next.addEventListener("click", async () => {
    if (!declining()) {
      const problems = page.questions.map((question, i) => markProblem(fields[i], question, draft.answers[question.id]));
      if (problems.some(Boolean)) {
        fields[problems.findIndex(Boolean)].scrollIntoView({ behavior: "smooth", block: "center" });
        return;
      }
      if (!last) {
        draft.page++;
        renderSurvey(holder, phase, survey);
        window.scrollTo(0, 0);
        return;
      }
    }
    next.disabled = true;
    next.textContent = t("sending");
    message.textContent = "";
    try {
      await post("/input", { token: session.token, type: "survey", phase, answers: submission(survey, draft.answers, declining()) });
      setTimeout(() => {
        if (status() === phase) {
          next.disabled = false;
          next.textContent = t("submit");
          message.textContent = t("rejected");
        }
      }, 6000);
    } catch {
      next.disabled = false;
      next.textContent = t("submit");
      message.textContent = t("sendFailed");
    }
  });
  holder.replaceChildren(...[
    el("h1", {}, localized(survey.title)),
    survey.pages.length > 1 ? el("p", { class: "note" }, t("page", draft.page + 1, survey.pages.length)) : null,
    page.title ? el("p", { class: "lead" }, localized(page.title)) : null,
    ...fields,
    el("div", { class: "nav" }, back, next, message),
  ].filter(Boolean));
  holder.onchange = () => { next.textContent = t(last || declining() ? "submit" : "next"); };
}

function declines(survey, answers) {
  return survey.pages.some((page) => page.questions.some((question) => {
    const answer = answers[question.id];
    return (question.options || []).some((option) => option.declinesConsent && answer && answer.selected === option.id);
  }));
}

/** Answers to send: everything answered, or nothing but the declining choice. */
function submission(survey, answers, declining) {
  const result = {};
  for (const page of survey.pages) {
    for (const question of page.questions) {
      const answer = answers[question.id];
      if (answer === undefined || question.type === "info") continue;
      if (declining && !(question.options || []).some((o) => o.declinesConsent && answer.selected === o.id)) continue;
      result[question.id] = answer;
    }
  }
  return result;
}

function problemOf(question, answer) {
  if (question.type === "info") return null;
  const empty = answer === undefined || (question.type === "matrix" && Object.keys(answer).length === 0);
  if (empty) return question.required ? t("required") : null;
  switch (question.type) {
    case "shortText":
    case "longText": {
      const max = question.maxLength || (question.type === "shortText" ? 50 : 10000);
      if (answer.length > max) return t("maxChars", max);
      return null;
    }
    case "number":
      if (!Number.isFinite(answer)) return t("number");
      if (!question.decimals && !Number.isInteger(answer)) return t("wholeNumber");
      if (question.min !== undefined && answer < question.min) return t("min", question.min);
      if (question.max !== undefined && answer > question.max) return t("max", question.max);
      return null;
    case "singleChoice":
    case "multiChoice":
    case "dropdown": {
      const selected = [].concat(answer.selected);
      if (selected.length === 0) return question.required ? t("required") : null;
      if (selected.includes("other") && !(answer.otherText || "").trim()) return t("otherMissing");
      return null;
    }
    case "matrix":
      if (question.required && Object.keys(answer).length < question.rows.length) return t("everyRow");
      return null;
    default:
      return null;
  }
}

function markProblem(field, question, answer) {
  const problem = problemOf(question, answer);
  field.classList.toggle("invalid", Boolean(problem));
  field.querySelector(".problem").textContent = problem || "";
  return problem;
}

function questionField(question, answers) {
  const set = (value) => {
    if (value === undefined) delete answers[question.id];
    else answers[question.id] = value;
  };
  const head = el("legend", { class: "title" }, localized(question.text), question.required ? el("span", { class: "required" }, " *") : null);
  const parts = [head];
  if (question.description) parts.push(el("div", { class: "description" }, localized(question.description)));
  const current = answers[question.id];
  switch (question.type) {
    case "info":
      break;
    case "shortText":
    case "longText": {
      const tag = question.type === "longText" ? "textarea" : "input";
      const input = el(tag, {
        type: tag === "input" ? "text" : null,
        rows: tag === "textarea" ? "4" : null,
        maxlength: String(question.maxLength || (question.type === "shortText" ? 50 : 10000)),
        "aria-label": surveyText(question.text),
      });
      input.value = current || "";
      input.addEventListener("input", () => set(input.value.trim() ? input.value : undefined));
      parts.push(input);
      break;
    }
    case "number": {
      const input = el("input", { type: "number", inputmode: question.decimals ? "decimal" : "numeric", min: question.min, max: question.max, step: question.decimals ? "any" : "1" });
      input.value = current ?? "";
      input.addEventListener("input", () => set(input.value === "" ? undefined : Number(input.value)));
      parts.push(input);
      break;
    }
    case "scale": {
      const row = el("div", { class: "scale" });
      for (let value = question.min ?? 1; value <= (question.max ?? 5); value++) {
        const button = el("button", { type: "button", class: "button", "aria-pressed": String(current === value) }, value);
        button.addEventListener("click", () => {
          set(answers[question.id] === value ? undefined : value);
          row.querySelectorAll("button").forEach((b) => b.setAttribute("aria-pressed", String(Number(b.textContent) === answers[question.id])));
          row.dispatchEvent(new Event("change", { bubbles: true }));
        });
        row.append(button);
      }
      parts.push(row);
      if (question.minLabel || question.maxLabel) {
        parts.push(el("div", { class: "scale-labels" }, el("span", {}, localized(question.minLabel)), el("span", {}, localized(question.maxLabel))));
      }
      break;
    }
    case "singleChoice":
    case "multiChoice":
      parts.push(...choiceInputs(question, current, set));
      break;
    case "dropdown": {
      const select = el("select", { "aria-label": surveyText(question.text) },
        el("option", { value: "" }, t("pick")),
        ...question.options.map((option) => el("option", { value: option.id }, surveyText(option.text).replaceAll("`", ""))));
      select.value = current ? current.selected : "";
      select.addEventListener("change", () => set(select.value ? { selected: select.value } : undefined));
      parts.push(select);
      break;
    }
    case "matrix": {
      const value = { ...(current || {}) };
      for (const rowDef of question.rows) {
        const row = el("div", { class: "scale" });
        for (const column of question.columns) {
          const button = el("button", { type: "button", class: "button", "aria-pressed": String(value[rowDef.id] === column.id) }, localized(column.text));
          button.addEventListener("click", () => {
            value[rowDef.id] = column.id;
            set({ ...value });
            row.querySelectorAll("button").forEach((b, i) => b.setAttribute("aria-pressed", String(question.columns[i].id === value[rowDef.id])));
            row.dispatchEvent(new Event("change", { bubbles: true }));
          });
          row.append(button);
        }
        parts.push(el("div", { class: "matrix-row" }, el("span", {}, localized(rowDef.text)), row));
      }
      break;
    }
  }
  parts.push(el("div", { class: "problem", role: "alert" }));
  return el("fieldset", { class: "question" }, ...parts);
}

function choiceInputs(question, current, set) {
  const multi = question.type === "multiChoice";
  const name = "q-" + question.id;
  const options = [...question.options];
  if (question.other) options.push({ id: "other", text: { [language]: t("other") } });
  let selected = current ? [].concat(current.selected) : [];
  let otherText = current && current.otherText ? current.otherText : "";
  const other = el("input", { type: "text", placeholder: t("other"), maxlength: "200", "aria-label": t("other") });
  other.value = otherText;
  const update = () => {
    if (selected.length === 0) return set(undefined);
    const answer = { selected: multi ? selected : selected[0] };
    if (selected.includes("other")) answer.otherText = otherText;
    set(answer);
  };
  const inputs = options.map((option) => {
    const input = el("input", { type: multi ? "checkbox" : "radio", name, value: option.id });
    input.checked = selected.includes(option.id);
    input.addEventListener("change", () => {
      if (multi) selected = input.checked ? [...selected, option.id] : selected.filter((id) => id !== option.id);
      else selected = [option.id];
      update();
    });
    return el("label", { class: "choice" }, input, el("span", {}, localized(option.text)));
  });
  other.addEventListener("input", () => {
    otherText = other.value;
    if (other.value && !selected.includes("other")) {
      selected = multi ? [...selected, "other"] : ["other"];
      inputs.forEach((label) => {
        const box = label.querySelector("input");
        box.checked = selected.includes(box.value);
      });
    }
    update();
  });
  return question.other ? [...inputs, other] : inputs;
}

// --- start

function start() {
  applyLanguage();
  render();
  // A link with a code, such as the QR code, joins right away; the join form shows any error. A
  // stored token for the code rejoins as the same participant.
  const code = digitsOnly(new URLSearchParams(location.search).get("code") || "");
  if (code.length === 6) app.querySelector("form.join").requestSubmit();
  setInterval(updateTimer, 200);
}

start();
