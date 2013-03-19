function doseq(fn, coll) {
    for (var key in coll) {
	fn(key);
    }
}

var Game = {
    display: null,
    map: {},
    engine: null,
    player: null,
    
    init: function() {
        this.display = new ROT.Display();
        document.body.appendChild(this.display.getContainer());
        
        this._generateMap();
        
        this.engine = new ROT.Engine();
        this.engine.addActor(this.player);
        this.engine.addActor(this.pedro);
        this.engine.start();
    },
    
    _generateMap: function() {
        var digger = new ROT.Map.Digger();
        var freeCells = [];
        
        var digCallback = function(x, y, value) {
            if (value) { return; }
            
            var key = x+","+y;
            this.map[key] = "·";
            freeCells.push(key);
        }
        digger.create(digCallback.bind(this));
        
        // this._generateBoxes(freeCells);
        marchgame.core.generate_boxes(this, freeCells);
        // this._drawWholeMap();
	marchgame.core.draw_whole_map(this);

        this.player = marchgame.core.create_being(Player, freeCells);
        this.pedro = marchgame.core.create_being(Pedro, freeCells);
    }
};

var Player = function(x, y) {
    this._x = x;
    this._y = y;
    this._draw();
}
    
Player.prototype.getSpeed = marchgame.core.get_speed;
    
Player.prototype.act = function() {
    Game.engine.lock();
    window.addEventListener("keydown", this);
}
    
Player.prototype.handleEvent = function(e) {
    var keyMap = {};
    keyMap[38] = 0;
    keyMap[33] = 1;
    keyMap[39] = 2;
    keyMap[34] = 3;
    keyMap[40] = 4;
    keyMap[35] = 5;
    keyMap[37] = 6;
    keyMap[36] = 7;

    var code = e.keyCode;

    if (code == 13 || code == 32) {
        this._checkBox();
        return;
    }

    /* one of numpad directions? */
    if (!(code in keyMap)) { return; }

    /* is there a free space? */
    var dir = ROT.DIRS[8][keyMap[code]];
    var newX = this._x + dir[0];
    var newY = this._y + dir[1];
    var newKey = newX + "," + newY;
    if (!(newKey in Game.map)) { return; }

    Game.display.draw(this._x, this._y, Game.map[this._x+","+this._y]);
    this._x = newX;
    this._y = newY;
    this._draw();
    window.removeEventListener("keydown", this);
    Game.engine.unlock();
}

Player.prototype._draw = function() {
    Game.display.draw(this._x, this._y, "@", "#ff0");
}    

Player.prototype._checkBox = function() {
    var key = this._x + "," + this._y;
    if (Game.map[key] != "*") {
        alert("There is no box here!");
    } else if (key == Game.ananas) {
        alert("Hooray! You found an ananas and won this game.");
        Game.engine.lock();
        window.removeEventListener("keydown", this);
    } else {
        alert("This box is empty :-(");
    }
}

Player.prototype.getX = function() { return this._x; }
 
Player.prototype.getY = function() { return this._y; }

var Pedro = function(x, y) {
    this._x = x;
    this._y = y;
    this._draw();
}
 
Pedro.prototype.getSpeed = marchgame.core.get_speed;
 
Pedro.prototype._draw = function() {
    Game.display.draw(this._x, this._y, "P", "red");
}

Pedro.prototype.act = function() {
    var x = Game.player.getX();
    var y = Game.player.getY();
    var path = [];
 
    var passableCallback = function(x, y) {
        return (x+","+y in Game.map);
    }
 
    var pathCallback = function(x, y) {
        path.push([x, y]);
    }
 
    var astar = new ROT.Path.AStar(x, y, passableCallback, {topology:4});
    astar.compute(this._x, this._y, pathCallback);

    path.shift(); /* remove Pedro's position */
    if (path.length <= 2) {
        Game.engine.lock();
        alert("Game over - you were captured by Pedro!");
    } else {
        x = path[0][0];
        y = path[0][1];
        Game.display.draw(this._x, this._y, Game.map[this._x+","+this._y]);
        this._x = x;
        this._y = y;
        this._draw();
    }
}
