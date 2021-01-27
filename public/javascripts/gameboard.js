
let size = 10
let colA = 0
let rowA = 0
let dir = "d"



class MatchField {

    constructor() {
        this.fields = []
        this.currentPlayerIndex = 0
    }

    createView () {
        let html = "<table>"
        let num = 0
        for(let row = 0; row < size; row++) {
            html += '<tr>'
            for(let col = 0; col < size; col++) {
                if(this.fields[num].colour === 0) {
                    html += '<td class="cells_blue cell">'
                    switch (this.fields[num].figName) {
                        case 'F':
                            html += '<span><img class="piece" src="/assets/images/character-flag.svg" alt="F"/></span>'
                            break;
                        case 'B':
                            html += '<span> <img class="piece" src="/assets/images/character-bomb.svg" alt="B"/> </span>'
                            break;
                        case 'M':
                            html+= '<span> <img class="piece" src="/assets/images/character-marshal.svg" alt="M"/> </span>'
                            break;
                        case '1':
                            html+='<span> <img class="piece" src="/assets/images/character-spy.svg" alt="1"/> </span>'
                            break;
                        case '2':
                            html+= '<span> <img class="piece" src="/assets/images/character-scout.svg" alt="2"/> </span>'
                            break;
                        case '3':
                            html+= '<span> <img class="piece" src="/assets/images/character-miner.svg" alt="3"/> </span>'
                            break;
                        case '4':
                            html+= '<span> <img class="piece" src="/assets/images/character-sergeant.svg" alt="4"/> </span>'
                            break;
                        case '5':
                            html+= '<span> <img class="piece" src="/assets/images/character-lieutenant.svg" alt="5"/> </span>'
                            break;
                        case '6':
                            html+= '<span> <img class="piece" src="/assets/images/character-captain.svg" alt="6"/> </span>'
                            break;
                        case '7':
                            html+= '<span> <img class="piece" src="/assets/images/character-major.svg" alt="7"/> </span>'
                            break;
                        case "8":
                            html+= '<span> <img class="piece" src="/assets/images/character-colonel.svg" alt="8"/> </span>'
                            break;
                        case "9":
                            html+= '<span> <img class="piece" src="/assets/images/character-general.svg" alt="9"/> </span>'
                            break;
                    }
                    html += '</td>'
                } else if (this.fields[num].colour === 1) {
                    html += '<td class="cells_red cell">'
                    switch (this.fields[num].figName) {
                        case 'F':
                            html += '<span><img class="piece" src="/assets/images/character-flag.svg" alt="F"/></span>'
                            break;
                        case 'B':
                            html += '<span> <img class="piece" src="/assets/images/character-bomb.svg" alt="B"/> </span>'
                            break;
                        case 'M':
                            html+= '<span> <img class="piece" src="/assets/images/character-marshal.svg" alt="M"/> </span>'
                            break;
                        case '1':
                            html+='<span> <img class="piece" src="/assets/images/character-spy.svg" alt="1"/> </span>'
                            break;
                        case '2':
                            html+= '<span> <img class="piece" src="/assets/images/character-scout.svg" alt="2"/> </span>'
                            break;
                        case '3':
                            html+= '<span> <img class="piece" src="/assets/images/character-miner.svg" alt="3"/> </span>'
                            break;
                        case '4':
                            html+= '<span> <img class="piece" src="/assets/images/character-sergeant.svg" alt="4"/> </span>'
                            break;
                        case '5':
                            html+= '<span> <img class="piece" src="/assets/images/character-lieutenant.svg" alt="5"/> </span>'
                            break;
                        case '6':
                            html+= '<span> <img class="piece" src="/assets/images/character-captain.svg" alt="6"/> </span>'
                            break;
                        case '7':
                            html+= '<span> <img class="piece" src="/assets/images/character-major.svg" alt="7"/> </span>'
                            break;
                        case "8":
                            html+= '<span> <img class="piece" src="/assets/images/character-colonel.svg" alt="8"/> </span>'
                            break;
                        case "9":
                            html+= '<span> <img class="piece" src="/assets/images/character-general.svg" alt="9"/> </span>'
                            break;
                    }
                    html += '</td>'
                } else {
                    html += '<td class="cells__green cell"> <span class="cell__empty"> </span> </td>'
                }
                num++;
            }
        html += '</tr>'
        }
        html += '</table>'
        return html
    }

    updateView() {
        const html = this.createView()
        $("#board").html(html)
    }

    move(dir, row, col) {
        $.ajax({
            method: "POST",
            url: "/move",
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "row": row,
                "col": col,
                "dir": dir
            }),

            success: (result) => {
                const {matchField, currentPlayer, currentPlayerIndex} = result
                this.updateMatchField(matchField)
                this.updateView()
                this.updateCurrentPlayer(currentPlayer, currentPlayerIndex)
            }
        });
    }

    updateMatchField(newFields) {
        this.fields = newFields
    }

    updateCurrentPlayer(currentPlayer, currentPlayerIndex){
        this.currentPlayerIndex = currentPlayerIndex
        document.getElementById("infoPlayer").innerHTML = currentPlayer + ", it's your turn!"
    }
}

$(document).on('click', '.cells_blue',(function () {
    colA = this.parentElement.rowIndex
    rowA = this.cellIndex

    // changes background color of selected cell:
    if(matchField.currentPlayerIndex === 0){
        $(".cell").removeClass('selectedCell');
        $(this).addClass('selectedCell');
    }
}))

$(document).on('click', '.cells_red',(function () {
    colA = this.parentElement.rowIndex
    rowA = this.cellIndex

    // changes background color of selected cell:
    if(matchField.currentPlayerIndex === 1){
        $(".cell").removeClass('selectedCell');
        $(this).addClass('selectedCell');
    }
}))

$(document).keydown(function(event){
    var key = event.which;
    switch(key) {
        case 37:
            dir = "l"
            console.log("left")
            // Key left.
            break;
        case 38:
            dir = "u"
            console.log("up")
            // Key up.
            break;
        case 39:
            dir = "r"
            console.log("right")
            // Key right.
            break;
        case 40:
            dir = "d"
            console.log("down")
            // Key down.
            break;
        case 65:
            attacking = !attacking
    }
    matchField.move(dir, rowA, colA)
});

function loadJson() {
    $.ajax({
        method: "GET",
        url: "/json",
        dataType: "json",

        success: function (result) {
            matchField = new MatchField();
            matchField.updateMatchField(result.matchField);
            matchField.updateView();
            matchField.updateCurrentPlayer(result.currentPlayer, result.currentPlayerIndex)
        }
    });
}

function connectWebSocket(){
    var webSocket = new WebSocket("ws://localhost:9000/websocket");

    webSocket.onopen = () => {
        console.log("Connected to WebSocket")
    };

    webSocket.onclose = () => function () {
        console.log('Connection with Websocket Closed!');
    };

    webSocket.onerror = function (error) {
        console.log('Error in Websocket Occured: ' + error);
    };

    webSocket.onmessage = function (e) {
        console.log("message");
        if (typeof e.data === "string") {
            let json = JSON.parse(e.data);
            let fields = json.matchField;
            let currentPlayerIndex = json.currentPlayerIndex;
            let currentPlayer = json.currentPlayer;
            console.log("fields: " + fields)
            console.log("playerIndex: " + currentPlayerIndex)

            matchField.updateMatchField(fields);
            matchField.updateView();
            matchField.updateCurrentPlayer(currentPlayer, currentPlayerIndex)
        }
    }
}

$( document ).ready(function() {
    console.log( "Document is ready, filling matchfield" );
    loadJson();
    connectWebSocket();
});
