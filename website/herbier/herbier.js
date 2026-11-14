// The Herbier page (written by data/scripts/build_herbier.py) is plain HTML; this script adds what the build cannot
// know: the month in progress. It washes that month's column of the flowering calendar in gold and fills the
// « En fleur en … » card (what flowers now, most planted first, or when the next flowers come), as the app does.

const MONTHS = ["janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"];
const SEASON_LEAVES = 6;

const month = Number(new Intl.DateTimeFormat("fr-FR", { month: "numeric", timeZone: "Europe/Paris" }).format(new Date()));
const rows = [...document.querySelectorAll(".calendar tbody tr")].map((row) => ({
  genus: row.dataset.genus,
  label: row.dataset.label,
  months: row.dataset.months.split(" ").map(Number),
  rank: Number(row.dataset.rank),
}));

/** The genera in flower in month `m`, most planted first. */
const flowering = (m) => rows.filter((row) => row.months.includes(m)).sort((a, b) => a.rank - b.rank);

/** « noisetier », « noisetier et orme », « noisetier, orme et if ». */
const frenchList = (items) => (items.length < 2 ? items.join("") : `${items.slice(0, -1).join(", ")} et ${items.at(-1)}`);

function leaf(genus) {
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "leaf");
  svg.setAttribute("viewBox", "0 0 100 100");
  const use = document.createElementNS("http://www.w3.org/2000/svg", "use");
  use.setAttribute("href", `#leaf-${genus}`);
  svg.append(use);
  return svg;
}

function markMonth() {
  for (const cell of document.querySelectorAll(`.calendar [data-month="${month}"]`)) cell.classList.add("now");
}

function fillSeason() {
  const card = document.getElementById("saison");
  if (!card || rows.length === 0) return;
  document.getElementById("saison-titre").textContent = `En fleur en ${MONTHS[month - 1]}`;
  const now = flowering(month);
  const text = document.getElementById("saison-texte");
  const names = document.getElementById("saison-noms");
  if (now.length > 0) {
    text.textContent = `${now.length} ${now.length === 1 ? "essence" : "essences"} en fleur`;
    document.getElementById("saison-feuilles").append(...now.slice(0, SEASON_LEAVES).map((row) => leaf(row.genus)));
    names.textContent = now.map((row) => row.label).join(" · ");
  } else {
    text.textContent = "Les arbres se reposent.";
    const next = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11].map((step) => ((month - 1 + step) % 12) + 1).find((m) => flowering(m).length > 0);
    if (next) {
      const list = frenchList(flowering(next).map((row) => row.label.toLowerCase()));
      names.textContent = `Prochaines floraisons dès ${MONTHS[next - 1]} : ${list}.`;
    }
  }
  card.hidden = false;
}

document.documentElement.classList.add("js");
markMonth();
fillSeason();
