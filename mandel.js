// $Id$
/*
* Mandel.js v1.1
* created by matortheeternal
* 04/16/2014
*
* Usage:
* /cs mandel <blocktype> <mandeltype> <size>
*
* For help type:
* /cs mandel help
*
* Supported types:
*   sponge: Makes a Menger Sponge.  Iteration size 1-6.
*   dust: Makes Cantor Dust.  Iteration size 1-6.
*   koch: Makes a Quadratic Koch Surface.  Iteration size 1-6.
*   cross: Makes a Greek Cross Fractal.  Iteration size 1-6.
*   hilbert: Makes a Hilbert Curve.  Iteration size 1-5.
*   octahedron: Makes an Octahedron Fractal.  Iteration size 1-5.
*   dodecahedron: working...
*   box: Makes a mandelbox.  Size is dimensional value (5-255).
*   bulb: Makes a Mandelbulb.  Size is dimensional value (5-255).
*   cBulb: Makes a custom Mandelbulb (slower). Size is dimensional value (5-255).
*/
 
importPackage(Packages.java.io);
importPackage(Packages.java.awt);
importPackage(Packages.com.sk89q.worldedit);
importPackage(Packages.com.sk89q.worldedit.blocks);
 
// get data
var blocks = context.remember();
var session = context.getSession();
var region = session.getRegion();

// writer
var writer = null;
writer = new BufferedWriter(new FileWriter("MandelLog.txt"));
 
// process arguments
context.checkArgs(1, 8, "<blocktype> <mandeltype> <size> [arg4] [arg5] [arg6] [arg7] [arg8]");
var bt = argv[1];
var mandeltype = argv[2];
var d = argv[3];
var cg = "\u00a7b";
 
// process block type for non-palette fractals
if ((mandeltype != "bulb") && (mandeltype != "cBulb") && (mandeltype != "box") && (argv[1] != "help"))
    var blocktype = context.getBlock(argv[1]);
 
// set global variables
var pi = 3.141592653589793; // pi
var gr = (1 + Math.pow(5, 0.5))/2; // golden ratio
var volume = 0; // tracks number of blocks placed
var tfMult = 2.0;
var tfSub = 1.0;
var amplitude = 8;
var thetaAdj = 0.000001; // used to prevent division by 0
var phiAdj = 0.0000001; // used to prevent division by 0
var vs = "1.1";
var bthelp = "For blocktype put any valid block ID or name.\n";
var ithelp = "For iteration put an integer value 1 < n < ";
var pthelp = "For palette put any valid palette or blocktype.\n";
var szhelp = "For size put any integer value 10 < n < 256\n";
var palettelist = ["default", "wool", "plant", "nether", "mine", "city", "wooden",
                    "frozen", "hot", "rocky", "test", "tropic", "fire", "ocean", "grayscale",
                    "blue", "blackNwhite", "blueGold"];
var typelist = ["sponge", "dust", "koch", "cross", "hilbert", "octahedron", "bulb", "box", "cbulb"];
var exttypelist = ["cube", "plane", "pentagon", "sponge", "dust", "koch", "cross",
                   "hilbert", "octahedron", "dodecahedron", "dodec", "bulb", "box", "cbulb"];
var typehelp = ["\n"+cg+"Generates a Menger Sponge. \n"+
                cg+"Usage: /cs mandel <blocktype> sponge <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"6\n"+
                cg+"e.g. /cs mandel stone sponge 4, /cs mandel 18:5 sponge 3",
               
                "\n"+cg+"Generates Cantor Dust. \n"+
                cg+"Usage: /cs mandel <blocktype> dust <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"6\n"+
                cg+"e.g. /cs mandel glowstone dust 3, /cs mandel 129 dust 4",
               
                "\n"+cg+"Generates a Quadratic Koch Surface. \n"+
                cg+"Usage: /cs mandel <blocktype> koch <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"6\n"+
                cg+"e.g. /cs mandel grass koch 3, /cs mandel 49 koch 4",
               
                "\n"+cg+"Generates a Greek Cross Fractal. \n"+
                cg+"Usage: /cs mandel <blocktype> cross <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"8\n"+
                cg+"e.g. /cs mandel brick cross 3, /cs mandel 35:14 cross 4",
               
                "\n"+cg+"Generates a 3d Hilbert Curve. \n"+
                cg+"Usage: /cs mandel <blocktype> hilbert <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"6\n"+
                cg+"e.g. /cs mandel grass hilbert 3, /cs mandel 48 hilbert 4",
               
                "\n"+cg+"Generates an Octahedron Fractal. \n"+
                cg+"Usage: /cs mandel <blocktype> octahedron <iteration>\n"+
                cg+bthelp+
                cg+ithelp+"6\n"+
                cg+"e.g. /cs mandel ironblock octahedron 3, /cs mandel 22 octahedron 4",
                
                "\n"+cg+"Generates a Mandelbulb. \n"+
                cg+"Usage: /cs mandel <palette> bulb <size>\n"+
                cg+pthelp+
                cg+szhelp+
                cg+"e.g. /cs mandel test bulb 60, /cs mandel ocean bulb 100",
                
                "\n"+cg+"Generates a Mandelbox. \n"+
                cg+"Usage: /cs mandel <palette> box <size> <scale> <bailout>\n"+
                cg+" <itmin> <iterations> <zoom>\n"+
                cg+pthelp+
                cg+szhelp+
                cg+"For scale choose a positive or negative value 1.1 < abs(n) < 4\n"+
                cg+"For bailout choose a positive value that is around 2.5*scale.\n"+
                cg+"For itmin choose a positive value 2 < n < 8\n"+
                cg+"For iterations choose a positive value 2 < n < 17\n"+
                cg+"For zoom choose a positive value 2 < n < 20\n"+
                cg+"e.g. /cs mandel grayscale box 75 -1.75 3.8 4 5 4\n"+
                cg+"/cs mandel test box 50 2 6 4 5 8",
                
                "\n"+cg+"Generates a Custom Mandelbulb. \n"+
                cg+"Usage: /cs mandel <palette> cbulb <size> <power> <bailout>\n"+
                cg+" <itmin> <iterations>\n"+
                cg+pthelp+
                cg+szhelp+
                cg+"For power choose a positive value 1 < n < 33\n"+
                cg+"For bailout choose a positive value that is ~1024.\n"+
                cg+"For itmin choose a positive value 0 < n < 20\n"+
                cg+"For iterations choose a positive value 0 < n < 17\n"+
                cg+"e.g. /cs mandel fire cbulb 64 10 1024 4 12",
                
                ];
 
/*
* >>> Block Lists (palettes) <<<
* These are some palettes for use in generation of mandelbulbs and mandelboxes.  
* Lower locations in the lists are more likely to be used, as per the percentage
* distribution array provided below, which was concluded from data from a 50x50x50
* mandelbulb.
*
* Percentage distribution: [32.6%, 14.8%, 11.6%, 6.6%, 7.1%, 4.5%, 3.7%, 3.1%,
                             3.1%,   2.5%, 2.1%, 1.8%, 1.4%, 1.3%, 1.5%, 1.2%]
*
* When making your own palettes, I recommend against using gravity-affected blocks
* (e.g. sand, gravel, water, lava) at any percentage higher than 2%.  These blocks
* will cause major lag and will obscure the generated object from view.
*/
var defaultBlockList = [1, 3, 3, 24, 1, 4, 24, 24, 12, 1, 3, 2, 1, 3, 82, 2];
// [35:0] White Wool, [35:8] Light Gray Wool, [35:7] Gray Wool, [35:15] Black Wool,
// [35:14] Red Wool, [35:1] Orange Wool, [35:4] Yellow Wool, [35:5] Light Green Wool,
// [35:13] Green Wool, [35:9] Cyan Wool, [35:3] Light Blue Wool, [35:11] Blue Wool,
// [35:10] Purple Wool, [35:2] Violet Wool, [35:6] Pink Wool, [35:12] Brown Wool
var woolBlockList = ["35:0", "35:0", "35:0", "35:0", "35:0", "35:8", "35:8", "35:8",  
                     "35:8", "35:14", "35:14", "35:6", "35:4", "35:5", "35:11", "35:3"];
// [2] Grass, [17:0] Oak Wood, [17:1] Spruce Wood, [17:3] Jungle Wood, [17:4] Acacia Wood,
// [17:5] Dark Oak Wood, [18:0] Oak Leaves, [18:1] Spruce Leaves, [18:3] Jungle Leaves,
// [18:4] Acacia Leaves, [18:5] Dark Oak Leaves, [106] Vines
//var plantBlockList = [2, "18:0", "18:1", 2, "18:2", "18:3", 2, "17:1", 2, "17:0", "17:3", 2, "17:1", 2, 2, 2];
var plantBlockList = [2, 2, "18:0", "17:0", "18:1", "17:1", "18:3", "17:3", "18:4", "17:4", "18:5", "17:5", 106, 106, 2, 2];
// [87] Netherrack, [49] Obsidian, [7] Bedrock, [89] Glowstone, [88] Soul Sand, [13] Gravel,
var netherBlockList = [87, 87, 87, 49, 7, 7, 87, 49, 87, 87, 87, 89, 89, 88, 13, 13];
// [1] Stone, [3] Dirt, [13] Gravel,[16] Coal, [15] Iron, [73] Redstone, [14] Gold, [56] Diamond, [129] Emerald
var mineBlockList = [1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 13, 16, 15, 73, 14, 56];
// [1] Stone, [20] Glass, [44:0] Stone Slab, [44:5] Stone Bricks Slab, [43:0] Double Stone Slab,
// [42] Iron, [45] Bricks, [44:4] Bricks Slab, [35:3] Light Blue Wool, [35:11] Blue Wool
var cityBlockList = [1, 20, 42, "44:0", "44:5", "43:0", 1, 42, 20, 1, 20, 1, "35:3", "35:11", 45, "44:4"];
// [5:0] Oak Wood Planks, [5:1] Spruce Wood Planks, [5:3] Jungle Wood Planks, [5:4] Acacia Wood Planks,
// [5:5] Dark Oak Wood Planks, [17:0] Oak Wood, [17:1] Spruce Wood, [17:3] Jungle Wood,
// [17:4] Acacia Wood, [17:5] Dark Oak Wood, [85] Fence, [53] Oak Wood Stairs, [134] Spruce Wood Stairs,
// [135] Birch Wood Stairs, [136] Jungle Wood Stairs
var woodenBlockList = ["5:0", "5:1", "5:3", "5:4", "5:5", 85, 85, "17:0", "17:1", "17:3", "17:4", "17:5", 53, 134, 135, 136];
// [79] Ice Block, [20] Glass, [35:3] Light Blue Wool, [35:9] Cyan Wool, [35:11] Blue Wool,
// [80] Snow Block, [22] Lapus Lazuli Block
var frozenBlockList = [79, 79, 20, 79, "35:3", "35:9", "35:11", 80, 22, 79, 79, 79, 79, 79, 79, 79];
// [152] Redstone Block, [35:14] Red Wool, [35:1] Orange Wool, [35:4] Yellow Wool, [41] Gold Block
// [87] Netherrack, [89] Glowstone
var hotBlockList = [152, "35:14", "35:1", "35:4", 41, 87, 87, 87, 152, "35:14", "35:1", "35:4", 87, 89, 152, 152, 152];
// [1] Stone, [4] Cobblestone, [7] Bedrock, [13] Gravel, [35:7] Dark Gray Wool, [35:8] Light Gray Wool,
// [43:0] Double Stone Slab, [43:5] Stone Bricks Slab, [98:3] Chiseled Stone, [82] Clay, [16] Coal Vein
var rockyBlockList = [1, 4, 7, 1, "35:7", "35:8", "43:0", "43:5", "98:3", 82, 1, 4, 7, 13, "35:7", 16];
// [35:0] White Wool, [35:8] Light Gray Wool, [35:7] Gray Wool, [35:15] Black Wool,
// [35:14] Red Wool, [35:1] Orange Wool, [35:4] Yellow Wool, [35:5] Light Green Wool,
// [35:13] Green Wool, [35:9] Cyan Wool, [35:3] Light Blue Wool, [35:11] Blue Wool,
// [35:10] Purple Wool, [35:2] Violet Wool, [35:6] Pink Wool, [35:12] Brown Wool
var testBlockList = ["35:0", "35:8", "35:7", "35:15", "35:14", "35:1", "35:4", "35:5",
                     "35:13", "35:9", "35:3", "35:11", "35:10", "35:2", "35:6", "35:12"];
// tropic wool
var tropicWoolBlockList = ["35:0", "35:8", "35:7", "35:13", "35:3", "35:11", "35:10", "35:2",
                      "35:0", "35:8", "35:7", "35:13", "35:3", "35:11", "35:10", "35:2"];
// fire wool
var fireWoolBlockList = ["35:14", "35:1", "35:4", "35:6", "35:0", "35:8", "35:7", "35:15",
                      "35:14", "35:1", "35:4", "35:6", "35:0", "35:8", "35:7", "35:15"];
// ocean wool
var oceanWoolBlockList = ["35:11", "35:3", "35:9", "35:13", "35:5", "35:10", "35:7", "35:15",
                      "35:11", "35:3", "35:9", "35:13", "35:5", "35:10", "35:7", "35:15"];
// grayscale
var grayscaleBlockList = ["35:0", "35:8", 1, "43:8", 4, "35:7", 7, "35:15",
                          "35:0", "35:8", 1, "43:8", 4, "35:7", 7, "35:15"];
// blue
var blueBlockList = ["35:11", "35:3", "35:9", 22, "35:0", "35:8", "35:7", "35:15",
                     "35:11", "35:3", "35:9", 22, "35:0", "35:8", "35:7", "35:15"];
// blackNwhite
var blackNwhiteBlockList = ["35:0", "35:15", 42, 49, "43:7", 173, 80, "35:7",
                            "35:0", "35:15", 42, 49, "43:7", 173, 80, "35:7"];
// blueGold
var blueGoldBlockList = ["35:11", "35:3", "35:9", 172, "159:1", "35:1", "35:4", 41,
                         24, 42, 80, "35:0", "35:0", "35:0", "35:0", "35:0"];
                           
                           
// process palette choice
if (bt.equals("default"))
    var activeBlockList = defaultBlockList
else if (bt.equals("wool"))
    var activeBlockList = woolBlockList
else if (bt.equals("plant"))
    var activeBlockList = plantBlockList
else if (bt.equals("nether"))
    var activeBlockList = netherBlockList
else if (bt.equals("mine"))
    var activeBlockList = mineBlockList
else if (bt.equals("city"))
    var activeBlockList = cityBlockList
else if (bt.equals("wooden"))
    var activeBlockList = woodenBlockList
else if (bt.equals("frozen"))
    var activeBlockList = frozenBlockList
else if (bt.equals("hot"))
    var activeBlockList = hotBlockList
else if (bt.equals("rocky"))
    var activeBlockList = rockyBlockList
else if (bt.equals("test"))
    var activeBlockList = testBlockList
else if (bt.equals("tropic"))
    var activeBlockList = tropicWoolBlockList
else if (bt.equals("fire"))
    var activeBlockList = fireWoolBlockList
else if (bt.equals("ocean"))
    var activeBlockList = oceanWoolBlockList
else if (bt.equals("grayscale"))
    var activeBlockList = grayscaleBlockList
else if (bt.equals("blue"))
    var activeBlockList = blueBlockList
else if (bt.equals("blackNwhite"))
    var activeBlockList = blackNwhiteBlockList
else if (bt.equals("blueGold"))
    var activeBlockList = blueGoldBlockList;
else if (!bt.equals("help"))
    var blocktype = context.getBlock(argv[1]);
 
// this is where we use mandeltype to decide what to make
function main() {
    if (argv[1] == "help") {
         if (argv.length < 3) {
             context.print("\n"+cg+"Mandel.js v"+vs);
             var suptypes = "Supported types: ";
             for (var i = 0; i < typelist.length; i++) {
                 if (i != 0)
                     suptypes += ", "+cg;
                 suptypes += typelist[i];
             }
             context.print(cg+suptypes);
             context.print(cg+"Use /cs mandel help <type> with a supported type to get "+
                           cg+"information on how to use that type.")
             context.print(cg+"Use /cs mandel help palettes to see a list of valid palettes.");
         }
         else {
             if (argv[2] == "palettes") {
                  var palettes = "\n"+cg+"Palettes: ";
                  for (var i = 0; i < palettelist.length; i++) {
                      if (i != 0)
                          palettes += ", "+cg;
                      palettes += palettelist[i];
                  }
                  context.print(palettes);
             }
             else {
                  for (var i = 0; i < typelist.length; i++) {
                      if (argv[2] == typelist[i]) {
                          context.print(typehelp[i]);
                          break;
                      }
                  }
             }
         }
         writer.close();
         return;
    }
    else if (mandeltype == "cube") {
        context.print("Generating a "+d+"x"+d+"x"+d+" Cube... \n");
        cube(d, 0, 0, 0);
    }
    else if (mandeltype == "plane") {
        context.print("Generating a plane at angle "+d);
        var dx = region.getMaximumPoint().getX() - region.getMinimumPoint().getX();
        var dy = region.getMaximumPoint().getY() - region.getMinimumPoint().getY();
        var dz = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ();
        var vec1 = new Vector(dx, dy, dz);
        d = d2r(d);
        //var angle1 = Math.acos(dx/dz);
        //var angle2 = 90 - angle1;
        var vec2 = rot(vec1, 1, pi/2, 1);
        var vec2 = rot(vec2, 2, d, 1);
        if (arg4 > 0)
            var vec2 = rot(vec2, 1, arg4, 1);
        context.print("vec1 = ("+vec1.getX()+", "+vec1.getY()+", "+vec1.getZ()+")");
        context.print("vec2 = ("+vec2.getX()+", "+vec2.getY()+", "+vec2.getZ()+")");
        plane(vec1, vec2);
    }
    else if (mandeltype == "pentagon") {
        context.print("Generating a pentagon at angle "+d);
        var dx = region.getMaximumPoint().getX() - region.getMinimumPoint().getX();
        var dy = region.getMaximumPoint().getY() - region.getMinimumPoint().getY();
        var dz = region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ();
        var vec0 = new Vector(region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ);
        var vec1 = new Vector(dx, dy, dz);
        d = d2r(d);
        var vec2 = rot(vec1, 1, pi/2, 1);
        var vec2 = rot(vec2, 2, d, 1);
        if (arg4 > 0)
            var vec2 = rot(vec2, 1, arg4, 1);
        context.print("vec1 = ("+vec1.getX()+", "+vec1.getY()+", "+vec1.getZ()+")");
        context.print("vec2 = ("+vec2.getX()+", "+vec2.getY()+", "+vec2.getZ()+")");
        pentagon(vec0, vec1, vec2);
    }
    else if (mandeltype == "sponge") {
        if (d > 5) {
            context.print("Can't generate a level "+d+" Menger Sponge!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Menger Sponge... \n");
        d = Math.pow(3, d);
        sponge(d, 0, 0, 0);
    }
    else if (mandeltype == "dust") {
        if (d > 5) {
            context.print("Can't generate level "+d+" Cantor Dust!  (Too big)");
            return;
        }
        context.print("Generating level "+d+" Cantor Dust... \n");
        d = Math.pow(3, d);
        dust(d, 0, 0, 0);
    }
    else if (mandeltype == "koch") {
        if (d > 5) {
            context.print("Can't generate a level "+d+" Quadratic Koch Surface!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Quadratic Koch Surface... \n");
        d = Math.pow(3, d);
        koch(d, 0, 0, 0, 0);
    }
    else if (mandeltype == "cross") {
        if (d > 7) {
            context.print("Can't generate a level "+d+" Greek Cross Fractal!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Greek Cross Fractal... \n");
        d = Math.pow(2, parseInt(d) + 1) - 1;
        cross(d, 0, 0, 0, 3, 0);
    }
    else if (mandeltype == "hilbert") {
        if (d > 5) {
            context.print("Can't generate a level "+d+" Hilbert Curve!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Hilbert Curve... \n");
        hilbert(d);
    }
    else if (mandeltype == "octahedron") {
        if (d > 5) {
            context.print("Can't generate a level "+d+" Octahedron Fractal!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Octahedron Fractal... \n");
        d = Math.pow(2, parseInt(d) + 2) - 1;
        context.print("  Real size: "+d+"x"+d+"x"+d+"\n");
        octahedron(d, 0, 0, 0);
    }
    else if (mandeltype == "dodecahedron") {
        if (d > 255) {
            context.print("Can't generate a "+d+"x"+d+"x"+d+" Dodecahedron!  (Too big)");
            return;
        }
        context.print("Generating a "+d+"x"+d+"x"+d+" Dodecahedron... \n");
        sdodec(d, 0, 0, 0);
    }
    else if (mandeltype == "dodec") {
        if (d > 3) {
            context.print("Can't generate a level "+d+" Dodecahedron Fractal!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Dodecahedron Fractal... \n");
        d = 30 * Math.pow(2 + gr, d - 1);
        context.print("Scale = "+d+"x"+d+"x"+d);
        dodecahedron(d, 0, 0, 0);
    }
    /*else if (mandeltype == "turtle") {
        if (d > 6) {
            context.print("Can't generate a level "+d+" Hilbert Curve!  (Too big)");
            return;
        }
        context.print("Generating a level "+d+" Hilbert Curve... \n");
        hilbertTurtle(d);
    }*/
    else if (mandeltype == "bulb") {
        if (d > 255) {
            context.print("Can't generate a "+d+"x"+d+"x"+d+" Mandelbulb!  (Too big)");
            return;
        }
        context.print("Generating a "+d+"x"+d+"x"+d+" Mandelbulb... \n");
        bulb();
    }
    else if (mandeltype == "box") {
        if (d > 255) {
            context.print("Can't generate a "+d+"x"+d+"x"+d+" Mandelbox!  (Too big)");
            return;
        }
        context.print("Generating a "+d+"x"+d+"x"+d+" Mandelbox... \n");
        box();
    }
    else if (mandeltype == "cbulb") {
        if (d > 255) {
            context.print("Can't generate a "+d+"x"+d+"x"+d+" Mandelbulb!  (Too big)");
            return;
        }
        context.print("Generating a custom "+d+"x"+d+"x"+d+" Mandelbulb... \n");
        cBulb();
    }
    context.print("Placed "+volume+" blocks!");
    writer.close();
}
 
// making a basic cube
function cube(d0, wOffset, hOffset, lOffset) {
    for (var h = 0; h < d0; h++) {
        for (var w = 0; w < d0; w++) {
            for (var l = 0; l < d0; l++) {
                var vec = new Vector(
                    region.getMinimumPoint().getX() + w + wOffset,
                    region.getMaximumPoint().getY() + h + hOffset,
                    region.getMinimumPoint().getZ() + l + lOffset);
                blocks.setBlock(vec, blocktype);
                volume++;
            }
        }
    }
}
 
// making a plane from two vectors
function plane(vec1, vec2) {
    var v1mag = mag(vec1);
    var v2mag = mag(vec2);
    // 2x resolution to reduce aliasing
    for (var i = 0; i < 2*v1mag; i++) {
        for (var j = 0; j < 2*v2mag; j++) {
            var iPer = i/(2*v1mag);
            var jPer = j/(2*v2mag);
            var vec = new Vector(
                region.getMinimumPoint().getX() + iPer*vec1.getX() + jPer*vec2.getX(),
                region.getMinimumPoint().getY() + iPer*vec1.getY() + jPer*vec2.getY(),
                region.getMinimumPoint().getZ() + iPer*vec1.getZ() + jPer*vec2.getZ());
            blocks.setBlock(vec, blocktype);
        }
    }
    volume = v1mag * v2mag;
    volume = volume - volume % 1;
}
 
// making a pentagon from three vectors
function pentagon(vec0, vec1, vec2) {
    var v1mag = mag(vec1);
    var v2mag = mag(vec2);
   
    for (var i = -v1mag; i <= v1mag; i++) {
        for (var j = -v2mag; j <= v2mag; j++) {
            var iPer = i/v1mag;
            var jPer = j/v2mag;
            var L1 = Math.tan(d2r(36))*iPer + 1;
            var L2 = 0 - Math.tan(d2r(36))*iPer + 1;
            var L3 = Math.tan(d2r(72))*iPer - 2.61833988749894848;
            var L4 = 0 - Math.tan(d2r(72))*iPer - 2.61833988749894848;
            var L5 = 0 - Math.sin(d2r(54));
            if ((jPer > L1) || (jPer > L2) || (jPer < L3) || (jPer < L4) || (jPer < L5)) {
                continue;
            }
            var vec = new Vector(
                vec0.getX() + iPer*vec1.getX() + jPer*vec2.getX(),
                vec0.getY() + iPer*vec1.getY() + jPer*vec2.getY(),
                vec0.getZ() + iPer*vec1.getZ() + jPer*vec2.getZ());
            blocks.setBlock(vec, blocktype);
        }
    }
}
 
// making a menger sponge
function sponge(d0, wOffset, hOffset, lOffset) {
    // return if d0 = 1
    if (d0 == 1)
    {
        var vec = new Vector(
            region.getMinimumPoint().getX() + wOffset,
            region.getMaximumPoint().getY() + hOffset,
            region.getMinimumPoint().getZ() + lOffset);
        blocks.setBlock(vec, blocktype);
        volume++;
        return;
    }
   
    // 1/3 and 2/3 of side length
    var d1 = d0/3;
   
    // recursion loop
    for (var h = 0; h < 3; h++) {
        for (var w = 0; w < 3; w++) {
            if ((h == 1) && (w == 1))
                continue;
            for (var l = 0; l < 3; l++) {
                if (((h == 1) && (l == 1)) || ((w == 1) && (l == 1)))
                    continue;
                // recursion
                if (d0 > 3)
                    sponge(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset)
                else {
                    var vec = new Vector(
                        region.getMinimumPoint().getX() + w + wOffset,
                        region.getMaximumPoint().getY() + h + hOffset,
                        region.getMinimumPoint().getZ() + l + lOffset);
                    blocks.setBlock(vec, blocktype);
                    volume++;
                }
            }
        }
    }
}
 
// making cantor dust
function dust(d0, wOffset, hOffset, lOffset) {
    // return if d0 = 1
    if (d0 == 1)
    {
        var vec = new Vector(
            region.getMinimumPoint().getX() + wOffset,
            region.getMaximumPoint().getY() + hOffset,
            region.getMinimumPoint().getZ() + lOffset);
        blocks.setBlock(vec, blocktype);
        volume++;
        return;
    }
   
    // 1/3 of side length
    var d1 = d0/3;
   
    // recursion loop
    for (var h = 0; h < 3; h++) {
        if (h == 1)
            continue;
        for (var w = 0; w < 3; w++) {
            if (w == 1)
                continue;
            for (var l = 0; l < 3; l++) {
                if (l == 1)
                    continue;
                // recursion
                if (d0 > 3)
                    dust(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset)
                else {
                    var vec = new Vector(
                        region.getMinimumPoint().getX() + w + wOffset,
                        region.getMaximumPoint().getY() + h + hOffset,
                        region.getMinimumPoint().getZ() + l + lOffset);
                    blocks.setBlock(vec, blocktype);
                    volume++;
                }
            }
        }
    }
}
 
// quadratic koch surface recursion master function
function kochRe(d1, wOffset, hOffset, lOffset, mode, wS, hS, lS) {
    var wR = 1;
    var hR = 1;
    var lR = 1;
    // define loop ranges based on input wS, hS, lS
    if (wS != 1)
        wR = 0;
    if (hS != 1)
        hR = 0
    if (lS != 1)
        lR = 0;
    // surrounding spawn
    for (var w = (wS - wR); w < (wS + wR + 1); w++) {
        for (var h = (hS - hR); h < (hS + hR + 1); h++) {
            for (var l = (lS - lR); l < (lS + lR + 1); l++) {
                if ((l != lS) || (h != hS) || (w != wS))
                    koch(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset, mode);
            }
        }
    }
    // subspawn
    if (hS <= 1)
        koch(d1, wS*d1 + wOffset, (hS + 1)*d1 + hOffset, lS*d1 + lOffset, 0);
    if ((hS >= 1) && (mode >= 1))
        koch(d1, wS*d1 + wOffset, (hS - 1)*d1 + hOffset, lS*d1 + lOffset, 1);
    if (wS <= 1)
        koch(d1, (wS + 1)*d1 + wOffset, hS*d1 + hOffset, lS*d1 + lOffset, 2);
    if (wS >= 1)
        koch(d1, (wS - 1)*d1 + wOffset, hS*d1 + hOffset, lS*d1 + lOffset, 3);
    if (lS <= 1)
        koch(d1, wS*d1 + wOffset, hS*d1 + hOffset, (lS + 1)*d1 + lOffset, 4);
    if (lS >= 1)
        koch(d1, wS*d1 + wOffset, hS*d1 + hOffset, (lS - 1)*d1 + lOffset, 5);
}
 
// making a quadratic koch surface
function koch(d0, wOffset, hOffset, lOffset, mode) {
    // return if d0 = 1
    if (d0 == 1)
        return;
   
    // 1/3 and 2/3 of side length
    var d1 = d0/3;
   
    // use mode to recurse
    var h = 1;
    var w = 1;
    var l = 1;
    switch(mode)
    {
    case 0:
        h--;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 0, w, h, l);
        break;
    case 1:
        h++;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 1, w, h, l);
        break;
    case 2:
        w--;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 2, w, h, l);
        break;
    case 3:
        w++;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 3, w, h, l);
        break;
    case 4:
        l--;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 4, w, h, l);
        break;
    case 5:
        l++;
        cube(d1, w*d1 + wOffset, h*d1 + hOffset, l*d1 + lOffset);
        kochRe(d1, wOffset, hOffset, lOffset, 5, w, h, l);
        break;
    }
}
 
// making a greek cross fractal
function cross(d0, wOffset, hOffset, lOffset, scale, mode) {
    var d1 = (d0 - 1)/2;
    if ((Math.floor(d1) - d1 != 0) || (d1 < scale))
        return;
    // make cross
    if (mode != 1) {
        for (var w = 0; w < d0; w++) {
            if (w == d1)
                continue;
            var vec = new Vector(
                region.getMinimumPoint().getX() + w + wOffset,
                region.getMaximumPoint().getY() + d1 + hOffset,
                region.getMinimumPoint().getZ() + d1 + lOffset);
            blocks.setBlock(vec, blocktype);
            volume++;
        }
    }
    if (mode != 2) {
        for (var h = 0; h < d0; h++) {
            if (h == d1)
                continue;
            var vec = new Vector(
                region.getMinimumPoint().getX() + d1 + wOffset,
                region.getMaximumPoint().getY() + h + hOffset,
                region.getMinimumPoint().getZ() + d1 + lOffset);
            blocks.setBlock(vec, blocktype);
            volume++;
        }
    }
    if (mode != 3) {
        for (var l = 0; l < d0; l++) {
            if (l == d1)
                continue;
            var vec = new Vector(
                region.getMinimumPoint().getX() + d1 + wOffset,
                region.getMaximumPoint().getY() + d1 + hOffset,
                region.getMinimumPoint().getZ() + l + lOffset);
            blocks.setBlock(vec, blocktype);
            volume++;
        }
    }
    d1++;
    // recursion close
    cross(d1 - 1, 0 + wOffset, d1/2 + hOffset, d1/2 + lOffset, scale, 1);
    cross(d1 - 1, d1/2 + wOffset, 0 + hOffset, d1/2 + lOffset, scale, 2);
    cross(d1 - 1, d1/2 + wOffset, d1/2 + hOffset, 0 + lOffset, scale, 3);
    // recursion far
    cross(d1 - 1, d1 + wOffset, d1/2 + hOffset, d1/2 + lOffset, scale, 1);
    cross(d1 - 1, d1/2 + wOffset, d1 + hOffset, d1/2 + lOffset, scale, 2);
    cross(d1 - 1, d1/2 + wOffset, d1/2 + hOffset, d1 + lOffset, scale, 3);
}
 
// octahedron fractal
function octahedron(d0, wOffset, hOffset, lOffset) {
    var oscale = 7;
    var d1 = (d0 - 1)/2;
    var d2 = (d1 - 1)/2;
    // create octahedron when minimum scale reached
    if (d1 < oscale) {
        var width = 0;
        for (var h = 0; h < d0; h++) {
            for (var l = 0; l < d0; l++) {
                for (var w = 0; w < d0; w++) {
                    if (Math.abs(l - d1) + Math.abs(w - d1) <= width) {
                        var vect = new Vector(
                            region.getMinimumPoint().getX() + w + wOffset,
                            region.getMinimumPoint().getY() + h + hOffset,
                            region.getMinimumPoint().getZ() + l + lOffset);
                        blocks.setBlock(vect, blocktype);
                        volume++;
                    }
                }
            }
            if (h > d1 - 1)
                width--
            else
                width++;
        }
    }
    else { // recursion
        octahedron(d1, d2 + 1 + wOffset, d1 + 1 + hOffset, d2 + 1 + lOffset); // top octahedron
        octahedron(d1, d2 + 1 + wOffset, hOffset, d2 + 1 + lOffset); // bottom octahedron
        octahedron(d1, wOffset, d2 + 1 + hOffset, d2 + 1 + lOffset); // close right octahedron
        octahedron(d1, d2 + 1 + wOffset, d2 + 1 + hOffset, lOffset); // close left octahedron
        octahedron(d1, d2 + 1 + wOffset, d2 + 1 + hOffset, d1 + 1 + lOffset); // far right octahedron
        octahedron(d1, d1 + 1 + wOffset, d2 + 1 + hOffset, d2 + 1 + lOffset); // far left octahedron
    }
}
 
function sdodec(d0, xOffset, yOffset, zOffset) {
    var d1 = d0/2;
    var a = d1/1.401258538; // side length of dodecahedron, definition d1 = r_u
    var b = 1.113517364*a; // r_i of dodecahedron
    var c = a/(2*Math.cos(d2r(54))); // r_u of pentagon on dodecahedron
    var t = Math.tan(d2r(63.43494));
    var k = Math.sin(d2r(54))*t*b + c; // plane shift constant 1
    var m = b - Math.sin(d2r(54))*b; // plane shift constant 2
    var cont = false;
   
    var p = [0,0,0,0,0,0,0,0,0,0,0,0];
    for (var x = -d1; x < d1; x++) {
        for (var z = -d1; z < d1; z++) {
            for (var y = -d1; y < d1; y++) {
                // get plane value given x,z
                p[0] = -b;
                p[1] = t*z - k;
                p[2] = t*x + (2/5)*t*z - k;
                p[3] = 0 - t*x + (2/5)*t*z - k;
                p[4] = (4/5)*t*x - t*z - k;
                p[5] = 0 - (4/5)*t*x - t*z - k;
                p[6] = t*(z - m) + k;
                p[7] = t*x + (2/5)*t*(z - m) + k;
                p[8] = 0 - t*x + (2/5)*t*(z - m) + k;
                p[9] = (4/5)*t*x - t*(z - m) + k;
                p[10] = 0 - (4/5)*t*x - t*(z - m) + k;
                p[11] = b
                for (var i = 0; i < 6; i++) {
                    if (p[i] > y) {
                        cont = true;
                        break;
                    }
                }
                for (var i = 6; i < 12; i++) {
                    if (p[i] < y) {
                        cont = true;
                        break;
                    }
                }
                if (cont) {
                    cont = false;
                    continue;
                }
                var vec = new Vector(
                    region.getMinimumPoint().getX() + x + xOffset + d1,
                    region.getMaximumPoint().getY() + y + yOffset + d1,
                    region.getMinimumPoint().getZ() + z + zOffset + d1);
                blocks.setBlock(vec, blocktype);
                volume++;
            }
        }
    }
}
 
function hdodec(d0) {
    var d1 = d0/2;
    //var d0 = d0/1;
    var gr = (1 + Math.pow(5, 0.5))/2;
    var a = d1/1.113516364; // side length of dodecahedron, definition d1 = r_i
    var b = a/2 + a*Math.cos(d2r(72)); // vector length for pentagon plane unit vectors
   
    // center vector points to center of dodecahedron
    var center = new Vector(
        region.getMinimumPoint().getX() + d1,
        region.getMinimumPoint().getY() + d1,
        region.getMinimumPoint().getZ() + d1);
    // pointer vector points to origin of pentagons to be generated from center of dodecahedron
    var pointer = new Vector(0, -1.113516364*a, 0);
   
    // bottom pentagon
    var vec0 = new Vector(
        center.getX() + pointer.getX(),
        center.getY() + pointer.getY(),
        center.getZ() + pointer.getZ());
    var vec1 = new Vector(0, 0, b);
    var vec2 = new Vector(b, 0, 0);
    pentagon(vec0, vec1, vec2);
    blocks.setBlock(vec0, context.getBlock("35:14"));
    blocks.setBlock(add(vec0, vec1), context.getBlock("35:11"));
    blocks.setBlock(add(vec0, vec2), context.getBlock("35:4"));
   
    // bottom pentagons
        // 1
        pointer = rot(pointer, 0, d2r(-63.4349487), 1);
        pointer = rot(pointer, 1, d2r(-18), 1);
        vec0 = Vector(
            center.getX() + pointer.getX(),
            center.getY() + pointer.getY(),
            center.getZ() + pointer.getZ());
        blocks.setBlock(vec0, blocktype);
        var vectemp = rot(vec2, 0, d2r(-63.4349487), 1);
        vectemp = rot(vectemp, 1, d2r(-18), 1);
        vec2 = rot(vec1, 0, d2r(-63.4349487), 1);
        vec2 = rot(vec2, 1, d2r(-18), 1);
        vec1 = Vector(-vectemp.getX(), -vectemp.getY(), -vectemp.getZ());
        pentagon(vec0, vec1, vec2);
        blocks.setBlock(vec0, context.getBlock("35:14"));
        blocks.setBlock(add(vec0, vec1), context.getBlock("35:11"));
        blocks.setBlock(add(vec0, vec2), context.getBlock("35:4"));
        // 2-5
        for (var i = 2; i <= 5; i++) {
            pointer = rot(pointer, 1, d2r(-72), 1);
            vec0 = Vector(
                center.getX() + pointer.getX(),
                center.getY() + pointer.getY(),
                center.getZ() + pointer.getZ());
            blocks.setBlock(vec0, blocktype);
            vec1 = rot(vec1, 1, d2r(-72), 1);
            vec2 = rot(vec2, 1, d2r(-72), 1);
            pentagon(vec0, vec1, vec2);
        }
   
   
    // top pentagon
    pointer = Vector(0, 1.113516364*a, 0);
    vec0 = Vector(
        center.getX() + pointer.getX(),
        center.getY() + pointer.getY(),
        center.getZ() + pointer.getZ());
    blocks.setBlock(vec0, blocktype);
    vec1 = Vector(0, 0, -b);
    vec2 = Vector(-b, 0, 0);
    pentagon(vec0, vec1, vec2);
    blocks.setBlock(vec0, context.getBlock("35:14"));
    blocks.setBlock(add(vec0, vec1), context.getBlock("35:11"));
    blocks.setBlock(add(vec0, vec2), context.getBlock("35:4"));
   
    // top pentagons
        // 1
        pointer = rot(pointer, 0, d2r(63.4349487), 1);
        pointer = rot(pointer, 1, d2r(18), 1);
        vec0 = Vector(
            center.getX() + pointer.getX(),
            center.getY() + pointer.getY(),
            center.getZ() + pointer.getZ());
        blocks.setBlock(vec0, blocktype);
        vectemp = rot(vec2, 0, d2r(63.4349487), 1);
        vectemp = rot(vectemp, 1, d2r(18), 1);
        vec2 = rot(vec1, 0, d2r(63.4349487), 1);
        vec2 = rot(vec2, 1, d2r(18), 1);
        vec2 = Vector(-vec2.getX(), -vec2.getY(), -vec2.getZ());
        vec1 = Vector(vectemp.getX(), vectemp.getY(), vectemp.getZ());
        pentagon(vec0, vec1, vec2);
        blocks.setBlock(vec0, context.getBlock("35:14"));
        blocks.setBlock(add(vec0, vec1), context.getBlock("35:11"));
        blocks.setBlock(add(vec0, vec2), context.getBlock("35:4"));
        // 2-5
        for (var i = 2; i <= 5; i++) {
            pointer = rot(pointer, 1, d2r(-72), 1);
            vec0 = Vector(
                center.getX() + pointer.getX(),
                center.getY() + pointer.getY(),
                center.getZ() + pointer.getZ());
            blocks.setBlock(vec0, blocktype);
            vec1 = rot(vec1, 1, d2r(-72), 1);
            vec2 = rot(vec2, 1, d2r(-72), 1);
            pentagon(vec0, vec1, vec2);
        }
}
 
// dodecahedron fractal
function dodecahedron(d0, wOffset, hOffset, lOffset) {
    var scale = 1/(2 + gr);
    var d1 = Math.round(scale*d0);
    context.print("d0 = "+d0+"  d1 = "+d1);
   
    // make dodec or recurse
    if (d1 < 20)
        sdodec(d0, wOffset, hOffset, lOffset)
    else {
        // make 20 dodecahedrons at corners of current dodecahedron
        var ru = d1/2;
        var ri = (1.113517364*ru)/1.401258538;
        var rnu = scale*ru;
        var rni = scale*ri;
        var r18 = d2r(18);
        var r54 = d2r(54);
       
        // initial x,y,z for bottom layer
        var x = wOffset;
        var y = hOffset + (ru - ri);
        var z = lOffset;
       
        //x and z for bottom & top layers
        var x1 = x + (ru - rnu)*(1 - Math.cos(r18));
        var z1 = z + (ru - ri) - (rnu - rni);
        var x2 = x1 + 2 * ru * Math.cos(r54);
        var x3 = x1 - ru*(Math.cos(r18) - Math.cos(r54));
        var z3 = z1 + ru*(Math.sin(r54) + Math.sin(r18));
        var x4 = x3 + 2*Math.cos(r54)*ru;
        var x5 = x + 2*ru - 2*rnu;
        var z5 = z + ru - rnu;
       
        // bottom layer
        dodecahedron(d1, x1, y, z1);
        dodecahedron(d1, x2, y, z1);
        dodecahedron(d1, x3, y, z3);
        dodecahedron(d1, x4, y, z3);
        dodecahedron(d1, x5, y, z5);
       
        // intial y for top layer
        var y = y + 2*ri;
    }
}
 
// hilbert curve rotation functions
    // rotate around the x-axis in the positive y direction
    function xp(s) {
        s = s.split("F").join("."); // forward becomes down
        s = s.split("B").join("^"); // backward becomes up
        s = s.split("D").join("B"); // down becomes backward
        s = s.split("U").join("F"); // up becomes forward
        // return to standard notation
        s = s.split("^").join("U");
        s = s.split(".").join("D");
        return s;
    }
   
    // rotate around the x-axis in the negative y direction
    function xn(s) {
        s = s.split("F").join("^"); // forward becomes up
        s = s.split("B").join("."); // backward becomes down
        s = s.split("D").join("F"); // down becomes forward
        s = s.split("U").join("B"); // up becomes backward
        // return to standard notation
        s = s.split("^").join("U");
        s = s.split(".").join("D");
        return s;
    }
   
    // rotate around the y-axis in the positive x direction
    function yp(s) {
        s = s.split("F").join("<"); // forward becomes left
        s = s.split("B").join(">"); // backward becomes right
        s = s.split("R").join("F"); // right becomes forward
        s = s.split("L").join("B"); // left becomes backward
        // return to standard notation
        s = s.split("<").join("L");
        s = s.split(">").join("R");
        return s;
    }
   
    // rotate around the y-axis in the negative x direction
    function yn(s) {
        s = s.split("F").join(">"); // forward becomes right
        s = s.split("B").join("<"); // backward becomes left
        s = s.split("R").join("B"); // right becomes backward
        s = s.split("L").join("F"); // left becomes forward
        // return to standard notation
        s = s.split("<").join("L");
        s = s.split(">").join("R");
        return s;
    }
   
    // rotate around the z axis in the positive x direction
    function zp(s) {
        s = s.split("U").join("<"); // up becomes left
        s = s.split("D").join(">"); // down becomes right
        s = s.split("R").join("D"); // right becomes down
        s = s.split("L").join("U"); // left becomes up
        // return to standard notation
        s = s.split(">").join("R");
        s = s.split("<").join("L");
        return s;
    }
   
    // rotate around the z axis in the negative x direction
    function zn(s) {
        s = s.split("U").join(">"); // up becomes right
        s = s.split("D").join("<"); // down becomes left
        s = s.split("R").join("U"); // right becomes up
        s = s.split("L").join("D"); // left becomes down
        // return to standard notation
        s = s.split(">").join("R");
        s = s.split("<").join("L");
        return s;
    }
   
    // reverse string
    function rv(s) {
        s = s.split("").reverse().join("");
        s = s.split("B").join("+");
        s = s.split("L").join(">");
        s = s.split("U").join(".");
        s = s.split("F").join("B");
        s = s.split("R").join("L");
        s = s.split("D").join("U");
        // return to standard notation
        s = s.split("+").join("F");
        s = s.split(">").join("R");
        s = s.split(".").join("D");
        return s;
    }
// end of rotation functions
 
// hilbert curve resolve function (turns string into function call and result)
function resolve(s) {
    var func = s.substring(0, 2);
    var input = s.substring(3, s.length);
    if (func == "xp")
        return xp(input);
    if (func == "xn")
        return xn(input);
    if (func == "yp")
        return yp(input);
    if (func == "yn")
        return yn(input);
    if (func == "zp")
        return zp(input);
    if (func == "zn")
        return zn(input);
    if (func == "rv")
        return rv(input);
}
 
// making a 3d hilbert curve
function hilbert(d0) {
    var hScale = 2;
    var A = "F R B D F L B";
    var curve = "A";
    var rs = "rv(xp(!)) F zp(!) R ! B yn(xn(!)) D yn(xp(!)) F ! L zn(!) B rv(xn(!))";
   
    var iterations = 1;
    // iterate generation string
    while (iterations < d0) {
        curve = curve.replace(/\A/g, rs);
        curve = curve.replace(/\!/g, "A");
        iterations++;
    }
   
    // evaluate functions and axioms in generation string
    curve = curve.replace(/\A/g, A);
    //writer.write("curve is: \n\n"+curve);
    //writer.write("\n\n");
    while ((curve.indexOf("(") > 0) && (curve.indexOf(")") > 0)) {
        var s1 = curve.substring(0, curve.indexOf(")"));
        //writer.write("  s1 = "+s1+"\n");
        s2 = s1.substring(s1.lastIndexOf("("), s1.length);
        //writer.write("  s1 = "+s1+"\n");
        var s3 = curve.substr(s1.lastIndexOf(s2) - 2, 3 + s2.length);
        //writer.write("  "+ s3 + " resolves to: " + resolve(s3.substr(0, s3.length - 1)) + "!"+"\n");
        curve = curve.replace(s3, resolve(s3.substr(0, s3.length - 1)));
    }
    writer.write("\n\ncurve is: \n\n"+curve);
   
    // calculate maximum height
    var hMax = hScale;
    for (var i = 1; i < d0; i++)
        hMax = 2 * hMax + hScale;
    // make curve
    var w = region.getMinimumPoint().getX();
    var h = region.getMinimumPoint().getY() + Math.floor(hMax) + 2;
    var l = region.getMinimumPoint().getZ();
    var vec = new Vector(w, h, l);
    blocks.setBlock(vec, blocktype);
    volume++;
    for (var i = 0; i < curve.length; i++) {
        if (curve.charAt(i) == ' ')
            continue;
        if (curve.charAt(i) == 'F') {
            for (var j = 0; j < hScale; j++) {
                w++;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        if (curve.charAt(i) == 'B') {
            for (var j = 0; j < hScale; j++) {
                w--;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        if (curve.charAt(i) == 'L') {
            for (var j = 0; j < hScale; j++) {
                l--;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        if (curve.charAt(i) == 'R') {
            for (var j = 0; j < hScale; j++) {
                l++;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        if (curve.charAt(i) == 'U') {
            for (var j = 0; j < hScale; j++) {
                h++;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        if (curve.charAt(i) == 'D') {
            for (var j = 0; j < hScale; j++) {
                h--;
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
    }
}
 
// matrix multiplication
function matrixMultiply(a, b) {
    var result = [
        [0, 0, 0],
        [0, 0, 0],
        [0, 0, 0]
    ];
   
    result[0][0] = a[0][0]*b[0][0] + a[0][1]*b[1][0] + a[0][2]*b[2][0];
    result[0][1] = a[0][0]*b[0][1] + a[0][1]*b[1][1] + a[0][2]*b[2][1];
    result[0][2] = a[0][0]*b[0][2] + a[0][1]*b[1][2] + a[0][2]*b[2][2];
 
    result[1][0] = a[2][1]*b[0][0] + a[1][1]*b[1][0] + a[1][2]*b[2][0];
    result[1][1] = a[2][1]*b[0][1] + a[1][1]*b[1][1] + a[1][2]*b[2][1];
    result[1][2] = a[1][0]*b[0][2] + a[1][1]*b[1][2] + a[1][2]*b[2][2];
   
    result[2][0] = a[2][0]*b[0][0] + a[2][1]*b[1][0] + a[2][2]*b[2][0];
    result[2][1] = a[2][0]*b[0][1] + a[2][1]*b[1][1] + a[2][2]*b[2][1];
    result[2][2] = a[2][0]*b[0][2] + a[2][1]*b[1][2] + a[2][2]*b[2][2];
   
    return result;
}
 
// rotate around dimension dim
function rot(m, dim, a, mode) {
    switch(dim) {
        case 0:
            var n = [
                [1, 0, 0],
                [0, Math.cos(a), -Math.sin(a)],
                [0, Math.sin(a),  Math.cos(a)]
            ];
            break;
        case 1:
            var n = [
                [Math.cos(a), 0, Math.sin(a)],
                [0, 1, 0],
                [-Math.sin(a), 0, Math.cos(a)]
            ];
            break;
        case 2:
            var n = [
                [Math.cos(a), -Math.sin(a), 0],
                [Math.sin(a), Math.cos(a), 0],
                [0, 0, 1]
            ];
            break;
    }
   
    switch(mode) {
    case 0:
        return matrixMultiply(m, n);
        break;
    case 1:
        var vec = new Vector(
            m.getX() * n[0][0] + m.getY() * n[0][1] + m.getZ() * n[0][2],
            m.getX() * n[1][0] + m.getY() * n[1][1] + m.getZ() * n[1][2],
            m.getX() * n[2][0] + m.getY() * n[2][1] + m.getZ() * n[2][2]);
        return vec;
        break;
    }
}
 
// making a 3d hilbert curve with turtle method
function hilbertTurtle(d0) {
    /*var hScale = 3;
    var curve = "A";
    var rs = ".X>.F+X<F<AF.AFA";
    var rss = "F>F>F.F.F<F<F++";
   
    var iterations = 0;
    // iterate generation string
    while (iterations < d0) {
        curve = curve.replace(/\A/g, rs);
        curve = curve.replace(/\X/g, rss);
        curve = curve.replace(/\!/g, "A");
        curve = curve.replace(/\@/g, "X");
        iterations++;
    }
   
    //curve = curve.replace(/\A/g, "^<F^<F-F^>>F&F->>F+F>->");
    writer.write("curve is: \n\n"+curve);
    writer.write("\n\n");
   
    // calculate maximum height
    var hMax = hScale;
    for (var i = 1; i < d0; i++)
        hMax = 2 * hMax + 3;
    // make curve
    var w = region.getMinimumPoint().getX();
    var h = region.getMinimumPoint().getY() + Math.floor(hMax) + 2;
    var l = region.getMinimumPoint().getZ();
    var vec = new Vector(w, h, l);
    var orientation = new Vector(1,0,0);
    blocks.setBlock(vec, blocktype);
    volume++;
    for (var i = 0; i < arg4; i++) {
        // define axes
        var xaxis = 0;
        var yaxis = 0;
        var zaxis = 0;
        // writer
        writer.write("Char = "+curve.charAt(i)+"  orientation = ("+orientation.getX()+","+orientation.getY()+","+orientation.getZ()+")\n");
        // interpret F as DrawForward(hScale)
        if (curve.charAt(i) == "F") {
            for (var j = 0; j < hScale; j++) {
               
                vec = Vector(w, h, l);
                blocks.setBlock(vec, blocktype);
                volume++;
                if (volume % 10000 == 0)
                    context.print("    "+volume+" blocks placed... \n");
            }
        }
        // interpret > as y-axis rotation +90
        // interpret < as y-axis rotation -90
        else if (curve.charAt(i) == "<")
            orientation = rotate(orientation, 1, 90)
        else if (curve.charAt(i) == ">")
            orientation = rotate(orientation, 1, -90)
        // interpret + as z-axis rotation +90
        // interpret - as z-axis rotation -90
        else if (curve.charAt(i) == "+")
            orientation = rotate(orientation, 0, 90)
        else if (curve.charAt(i) == "-")
            orientation = rotate(orientation, 0, -90)
        // interpret ^ as x-axis rotation +90
        // interpret . as x-axis rotation -90
        else if (curve.charAt(i) == "^")
            orientation = rotate(orientation, 2, 90)
        else if (curve.charAt(i) == ".")
            orientation = rotate(orientation, 2, -90);
    }
    writer.close();*/
}
 
// making a quick mandelbulb
function bulb() {
    // set local variables
    var power = 8;
    var cutoff = 1024;
    var itMin = 4;
    var itMax = 20;
    var tfMult = 2;
    var tfSub = tfMult/2;
    // this data is used to skip interior points instead of going through iterations until itMax
    var mindata = [
        [0.331*d, 0.335*d, 0.331*d, 0.338*d, 0.339*d, 0.327*d, 0.338*d, 0.331*d, 0.335*d, 0.331*d],
        [0.333*d, 0.359*d, 0.329*d, 0.340*d, 0.337*d, 0.329*d, 0.340*d, 0.329*d, 0.359*d, 0.333*d],
        [0.319*d, 0.319*d, 0.328*d, 0.329*d, 0.329*d, 0.327*d, 0.329*d, 0.328*d, 0.339*d, 0.319*d],
        [0.345*d, 0.349*d, 0.342*d, 0.339*d, 0.349*d, 0.336*d, 0.339*d, 0.342*d, 0.349*d, 0.345*d],
        [0.331*d, 0.356*d, 0.329*d, 0.319*d, 0.338*d, 0.327*d, 0.319*d, 0.329*d, 0.356*d, 0.331*d],
        [0.335*d, 0.349*d, 0.332*d, 0.332*d, 0.339*d, 0.331*d, 0.332*d, 0.332*d, 0.349*d, 0.335*d],
        [0.327*d, 0.349*d, 0.328*d, 0.329*d, 0.338*d, 0.327*d, 0.329*d, 0.328*d, 0.349*d, 0.327*d],
        [0.342*d, 0.365*d, 0.336*d, 0.339*d, 0.352*d, 0.329*d, 0.339*d, 0.336*d, 0.365*d, 0.342*d],
        [0.328*d, 0.329*d, 0.328*d, 0.328*d, 0.329*d, 0.319*d, 0.328*d, 0.328*d, 0.329*d, 0.328*d],
        [0.338*d, 0.339*d, 0.334*d, 0.334*d, 0.339*d, 0.329*d, 0.334*d, 0.334*d, 0.339*d, 0.338*d]
    ];
   
    // loop through xyz space
    for (var x = 0; x < d; x++) {
        var xc = (tfMult * x)/d - tfSub;
        for (var y = 0; y < d; y++) {
            var yc = (tfMult * y)/d - tfSub;
            for (var z = 0; z < d; z++) {
                /* Continue if radius is less than stored mindata value.
                 * This saves a lot of time by not performing itMax iterations
                 * on locations that clearly won't have blocks. */
                var cVec = new Vector(x - (d/2), y - (d/2), z - (d/2));
                var r = mag(cVec);
                var i1 = Math.floor(theta(cVec)/(pi/10));
                var i2 = Math.floor((phi(cVec) + pi/2)/(pi/10));
                if (r < mindata[i1][i2])
                    continue;
               
                var zc = (tfMult * z)/d - tfSub;
               
                var iterations = -1;
                var C = new Vector(xc, yc, zc);
                var Z = new Vector(0, 0, 0);
               
                // iterate over vectors
                while ((mag(Z) <= cutoff) && (iterations < itMax)) {
                    Z = add(formula(Z, power), C);
                    iterations++;
                }
               
                // place block if cutoff reached in itMin -> itMax range
                if ((iterations >= itMin) && (iterations < itMax)) {
                    volume++;
                    if (volume % 10000 == 0)
                        context.print("    "+volume+" blocks placed... \n");
                    var vec = new Vector(
                        region.getMinimumPoint().getX() + x,
                        region.getMaximumPoint().getY() + y,
                        region.getMinimumPoint().getZ() + z);
                    if (activeBlockList.getSize != 0)
                        blocktype = context.getBlock(activeBlockList[iterations - itMin]);
                    blocks.setBlock(vec, blocktype);
                }
            }
        }
    }
}
 
// generate a custom mandelbulb
function cBulb() {
    // set local variables
    var power = argv[4] > 0 ? parseFloat(argv[4]) : 8;
    var bailout = argv[5] > 0 ? parseFloat(argv[5]) : 1024;
    var itMin = argv[6] > 0 ? parseInt(argv[6]) : 4;
    var itMax = argv[7] > 0 ? parseInt(argv[7]) + itMin : 16 + itMin;
    var tfMult = argv[8] > 0 ? parseFloat(argv[8]) : 2;
    var tfSub = tfMult/2;
   
    // loop through xyz space
    for (var x = 0; x < d; x++) {
        var xc = (tfMult * x)/d - tfSub;
        for (var y = 0; y < d; y++) {
            var yc = (tfMult * y)/d - tfSub;
            for (var z = 0; z < d; z++) {
                var zc = (tfMult * z)/d - tfSub;
               
                var iterations = -1;
                var C = new Vector(xc, yc, zc);
                var Z = new Vector(0, 0, 0);
               
                // iterate over vectors
                while ((mag(Z) <= bailout) && (iterations < itMax)) {
                    Z = add(formula(Z, power), C);
                    iterations++;
                }
               
                // place block if cutoff reached in itMin -> itMax range
                if ((iterations >= itMin) && (iterations < itMax)) {
                    volume++;
                    if (volume % 10000 == 0)
                        context.print("    "+volume+" blocks placed... \n");
                    var vec = new Vector(
                        region.getMinimumPoint().getX() + x,
                        region.getMaximumPoint().getY() + y,
                        region.getMinimumPoint().getZ() + z);
                    if (activeBlockList.getSize != 0)
                        blocktype = context.getBlock(activeBlockList[iterations - itMin]);
                    blocks.setBlock(vec, blocktype);
                }
            }
        }
    }
}
 
// making a mandelbox
function box() {
    var arg4 = argv[4] > -10 ? parseFloat(argv[4]) : 2;
    var arg5 = argv[5] > 0 ? parseFloat(argv[5]) : 4;
    var itMinBox = argv[6] > 0 ? parseInt(argv[6]) : 4;
    var itMaxBox = (argv[7] > 0) && (argv[7] < 17) ? parseInt(argv[7]) + itMinBox : 5 + itMinBox;
    var tfmult = argv[8] > 0 ? parseFloat(argv[8]) : 4;
    var tfsub = tfmult/2;
    for (var x = 0; x < d; x++) {
        var xc = (tfmult * x)/(d - 1) - tfsub;
        for (var y = 0; y < d; y++) {
            var yc = (tfmult * y)/(d - 1) - tfsub;
            for (var z = 0; z < d; z++) {
                var zc = (tfmult * z)/(d - 1) - tfsub;
 
                var iterations = -1;
                C = new Vector(xc, yc, zc);
                Z = new Vector(0, 0, 0);
 
                while ((mag(Z) < arg5) && (iterations < itMaxBox)) {
                    Z = add(mult(arg4, boxformula(Z)), C);
                    iterations++;
                }
               
                if ((iterations >= itMinBox) && (iterations < itMaxBox)) {
                    volume++;
                    if (volume % 10000 == 0)
                        context.print("    "+volume+" blocks placed... \n");
                    var vec = new Vector(
                        region.getMinimumPoint().getX() + x,
                        region.getMaximumPoint().getY() + y,
                        region.getMinimumPoint().getZ() + z);
                    if (activeBlockList.getSize != 0)
                        blocktype = context.getBlock(activeBlockList[iterations - itMinBox]);
                    blocks.setBlock(vec, blocktype);
                }
            }
        }
    }
}
 
// mandelbox vector growth formula
function boxformula(vec) {
    var x = vec.getX();
    var y = vec.getY();
    var z = vec.getZ();
   
    if (x > 1)
        x = 2 - x
    else if (x < -1)
        x = -2 - x;
       
    if (y > 1)
        y = 2 - y
    else if (y < -1)
        y = -2 - y;
   
    if (z > 1)
        z = 2 - z
    else if (z < -1)
        z = -2 - z;
   
    var output = new Vector(x, y, z);
    var m = mag(output);
   
    if (m < 0.5)
        output = mult(4, output)
    else if (m < 1)
        output = mult(1/(m*m), output);
   
    return output;
}
 
// arbitrary rotation treating input vector as z-axis.
function arot(vec, dim, a) {
    var m = mag(vec);
    // zaxis is unit vector in input vec direction
    var zaxis = new Vector(vec.getX()/m, vec.getY()/m, vec.getZ()/m);
   
    // xaxis is always in xz plane.
    if ((zaxis.getX()/abs(zaxis.getX())) + zaxis.getZ()/abs(zaxis.getZ()) == 0)
      var xaxis = new Vector(-zaxis.getX(), 0, xaxis.getZ())
    else
      var xaxis = new Vector(zaxis.getX(), 0, -zaxis.getZ());
   
    // yaxis is in "above" octant.
    if (zaxis.getY()/abs(zaxis.getY()) > 0)
      var yaxis = new Vector(-zaxis.getX(), 1 - zaxis.getY(), -zaxis.getZ())
    else
      var yaxis = new Vector(zaxis.getX(), 1 - zaxis.getY(), zaxis.getZ());
   
    // rotation matrices
    switch(dim) {
        case 0:
            var n = [
                [1, 0, 0],
                [0, Math.cos(a), -Math.sin(a)],
                [0, Math.sin(a),  Math.cos(a)]
            ];
            break;
        case 1:
            var n = [
                [Math.cos(a), 0, Math.sin(a)],
                [0, 1, 0],
                [-Math.sin(a), 0, Math.cos(a)]
            ];
            break;
        case 2:
            var n = [
                [Math.cos(a), -Math.sin(a), 0],
                [Math.sin(a), Math.cos(a), 0],
                [0, 0, 1]
            ];
            break;
    }
   
    // return rotated vector
    var result = new Vector(
        xaxis.getX() * n[0][2] + yaxis.getX()*n[1][2] + zaxis.getX() * n[2][2],
        xaxis.getY() * n[0][2] + yaxis.getY()*n[1][2] + zaxis.getY() * n[2][2],
        xaxis.getZ() * n[0][2] + yaxis.getZ()*n[1][2] + zaxis.getZ() * n[2][2]);
    return result;
}
 
// vector magnitude function
// returns the length of the vector
function mag(vec) {
    var x = vec.getX(), y = vec.getY(), z = vec.getZ();
    return Math.sqrt(x * x + y * y + z * z);
}
 
// vector addition function
// adds two vectors together
function add(v1, v2) {
    var x1 = v1.getX(), y1 = v1.getY(), z1 = v1.getZ();
    var x2 = v2.getX(), y2 = v2.getY(), z2 = v2.getZ();
    var output = new Vector(
        x1+x2,
        y1+y2,
        z1+z2);
   
    return output;
}
 
// multiplies a constant and a vector together
function mult(c, vec) {
    var x = vec.getX(), y = vec.getY(), z = vec.getZ();
    var output = new Vector(
        c * x,
        c * y,
        c * z);
   
    return output;
}
 
// mandelbulb vector growth formula
// if current vector magnitude > 1 and t is not too close to a multiple of (pi/2)
// this will give a vector of greater magnitude than the vector put in
// n, which is param, is the rate at which vector magnitude grows.
function formula(vec, n) {
    var t = theta(vec);
    var p = phi(vec);
    var k = Math.pow(mag(vec), n);
    var output = new Vector(
        k*Math.sin(n*t)*Math.cos(n*p),
        k*Math.sin(n*t)*Math.sin(n*p),
        k*Math.cos(n*t));
   
    return output;
}
 
// theta vector value (arccos)
function theta(vec) {
    var x = vec.getX(), y = vec.getY(), z = vec.getZ();
    return (Math.acos(z/(mag(vec) + thetaAdj)));
}
 
// phi vector value (arctan)
function phi(vec) {
    var x = vec.getX(), y = vec.getY(), z = vec.getZ();
    return (Math.atan(y/(x + phiAdj)));
}
 
// distance from center
// moves vector origin to center and calculates its magnitude
function rfc(x, y, z) {
    var vec = new Vector(x - (d/2), y - (d/2), z - (d/2));
    return mag(vec);
}
 
// degrees to radians
function d2r(degrees) {
    return (pi * degrees / 180);
}
 
// run main method
main();

