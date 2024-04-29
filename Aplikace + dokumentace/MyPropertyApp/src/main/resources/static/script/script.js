function sideMenu() {
    let side = document.getElementById("sideMenu-show");

    if (side.style.width === "65px") {
        side.style.width = '182px';
    } else {
        side.style.width = "65px";
    }
}

function topMenu() {
    let top = document.getElementById("topMenu-show");

    if (top.style.height === "0px") {
        top.style.height = '350px';
    } else {
        top.style.height = "0px";
    }
}


function toggleDisplay(id) {
    const otherWindows = ['notice', 'profil', 'calendar', 'apps'];

    otherWindows.forEach(windowId => {
        if (windowId !== id) {
            let elementToHide = document.getElementById(windowId);
            if (elementToHide) {
                elementToHide.style.display = 'none';
            }
        }
    });

    let element = document.getElementById(id);

    if (element.style.display === "none") {
        element.style.display = "block";
    } else {
        element.style.display = "none";
    }
}


const monthNames = ["January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
];

const calendarMonthYear = document.getElementById('calendar-month-year');
const calendarBody = document.getElementById('calendar-body');
const prevButton = document.getElementById('calendar-prev');
const nextButton = document.getElementById('calendar-next');

let currentDate = new Date();

function fillCalendar() {
    const firstDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    const lastDay = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);

    const monthDays = lastDay.getDate();
    let startDayOfWeek = firstDay.getDay();

    startDayOfWeek = (startDayOfWeek === 0) ? 6 : startDayOfWeek - 1;

    calendarMonthYear.textContent = `${monthNames[currentDate.getMonth()]} ${currentDate.getFullYear()}`;

    let calendarHTML = '';
    for (let i = 0; i < startDayOfWeek; i++) {
        calendarHTML += '<td class="empty"></td>'; // Třída "empty" pro prázdné buňky
    }

    for (let day = 1; day <= monthDays; day++) {
        const isToday = currentDate.getDate() === day && new Date().getMonth() === currentDate.getMonth() && new Date().getFullYear() === currentDate.getFullYear();
        calendarHTML += `<td class="${isToday ? 'today' : ''}">${day}</td>`;
        if ((day + startDayOfWeek) % 7 === 0) {
            calendarHTML += '</tr><tr>';
        }
    }

    for (let i = (startDayOfWeek + monthDays) % 7; i < 7 && i !== 0; i++) {
        calendarHTML += '<td class="empty"></td>'; // Třída "empty" pro prázdné buňky
    }

    calendarBody.innerHTML = '<tr>' + calendarHTML + '</tr>';
}

function goToPreviousMonth() {
    currentDate.setMonth(currentDate.getMonth() - 1);
    fillCalendar();
}

function goToNextMonth() {
    currentDate.setMonth(currentDate.getMonth() + 1);
    fillCalendar();
}

prevButton.addEventListener('click', goToPreviousMonth);
nextButton.addEventListener('click', goToNextMonth);

fillCalendar();


// Funkce pro filtrování možností výběru
function filterOptions(inputId, optionsContainerId) {
    const input = document.getElementById(inputId);
    const optionsContainer = document.getElementById(optionsContainerId);
    const filter = input.value.toLowerCase();
    const options = optionsContainer.getElementsByClassName('option');

    let visibleOptionExists = false;
    for (let option of options) {
        const text = option.textContent.toLowerCase();
        if (text.includes(filter)) {
            option.style.display = '';
            visibleOptionExists = true;
        } else {
            option.style.display = 'none';
        }
    }

    // Zobrazení nebo skrytí kontejneru s možnostmi
    optionsContainer.style.display = visibleOptionExists ? 'block' : 'none';
}

// Přiřazení události 'input'
document.getElementById('categoryInput').addEventListener('input', function() {
    filterOptions('categoryInput', 'categoryOptions');
    document.getElementById('locationOptions').style.display = 'none'; // Skryje kontejner s možnostmi pro lokace
});
document.getElementById('locationInput').addEventListener('input', function() {
    filterOptions('locationInput', 'locationOptions');
    document.getElementById('categoryOptions').style.display = 'none'; // Skryje kontejner s možnostmi pro kategorie
});

// Globální posluchač událostí pro kliknutí
document.addEventListener('click', function(e) {
    if (e.target.matches('#categoryInput')) {
        document.getElementById('categoryOptions').style.display = 'block';
        document.getElementById('locationOptions').style.display = 'none'; // Skryje kontejner s možnostmi pro lokace
    } else if (e.target.matches('#locationInput')) {
        document.getElementById('locationOptions').style.display = 'block';
        document.getElementById('categoryOptions').style.display = 'none'; // Skryje kontejner s možnostmi pro kategorie
    } else if (e.target.matches('.option')) {
        // Vyplnění vstupního pole a skrytí kontejneru
        const inputId = e.target.parentNode.id.replace('Options', 'Input');
        document.getElementById(inputId).value = e.target.getAttribute('data-value');
        e.target.parentNode.style.display = 'none';
    } else {
        // Skrytí obou kontejnerů s možnostmi
        document.getElementById('categoryOptions').style.display = 'none';
        document.getElementById('locationOptions').style.display = 'none';
    }
});

