package com.example.painter

import com.example.graph.*
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.*
import tornadofx.*
import javafx.scene.shape.QuadCurveTo;

class Arrow @JvmOverloads constructor(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    arrowHeadSize: Double = defaultArrowHeadSize,
    curved: Boolean = false
) :
    Path() {
    companion object {
        private const val defaultArrowHeadSize = 5.0
    }

    init {
        strokeProperty().bind(fillProperty())
        setFill(Color.BLACK)

        //Line
        getElements().add(MoveTo(startX, startY))

        if (curved == false)
            getElements().add(LineTo(endX, endY))
        else getElements().add(MoveTo(endX, endY))

        //ArrowHead
        val angle = Math.atan2(endY - startY, endX - startX) - Math.PI / 2.0
        val sin = Math.sin(angle)
        val cos = Math.cos(angle)
        //point1
        val x1 = (-1.0 / 2.0 * cos + Math.sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y1 = (-1.0 / 2.0 * sin - Math.sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        //point2
        val x2 = (1.0 / 2.0 * cos + Math.sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y2 = (1.0 / 2.0 * sin - Math.sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        getElements().add(LineTo(x1, y1))
        getElements().add(LineTo(x2, y2))
        getElements().add(LineTo(endX, endY))

    }
}
fun shortenedLine(line : Line, len: Double): Line{
    val x2 = line.endX
    val x1 = line.startX
    val y2 = line.endY
    val y1 = line.startY

    var dx: Double = x2 - x1
    var dy: Double = y2 - y1
    val length: Double = Math.sqrt(dx * dx + dy * dy)
    if (length > 0) {
        dx /= length
        dy /= length
    }
    dx *= length - len
    dy *= length - len
    val x3 = (x1 + dx)
    val y3 = (y1 + dy)
    return Line(x1, y1, x3, y3)

}
fun returnPoint(x1: Double, x2: Double, y1: Double, y2: Double, distance: Double, minus: Int): Pair<Double, Double>{
    val k = (x1 - x2)/(y2 - y1)
    val b = (y1 + y2)/2 - (x1 + x2)/2 * k
    val x1 = (x1 + x2) / 2
    val y1 = (y1 + y2) / 2
    var x3 = x1 + distance/Math.sqrt(1 + k*k) * minus
    var y3 = k * x3 + b
    return Pair(x3, y3)
}
class Painter{

    private fun paintVertex(v: Vertex, x: Double, y: Double, rad: Double = 25.0): StackPane {

        var circle =  Circle()
        circle.setRadius(rad)
        circle.setCenterX(x)
        circle.setCenterY(y)
        circle.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 2;");
        var lb = Label(v.getValue())
        lb.setStyle("-fx-font-smoothing-type: lcd; -fx-fill: white; -fx-font-size: 22pt;")
        var s = StackPane(circle, lb)
        s.layoutX = x
        s.layoutY = y
        s.maxHeight = 30.0
        s.maxWidth = 30.0
        return s
    }
    fun paintGraph(g: Graph): Pane{
        var p = Pane()
        p.maxHeight = 300.0
        p.maxWidth = 300.0
        val r = 200.0
        val num = g.getVertices().size
        var vwc = mutableListOf<Triple<Vertex, Double, Double>>() //vertices with coordinates
        for (i in 0 until num){
            val cntr = p.maxHeight / 2 + 100.0
            val circle_x = r * Math.cos(2 * Math.PI * i/num) + cntr
            val circle_y = r * Math.sin(2 * Math.PI * i/num) + cntr
            //p.add(paintVertex(g.getVertices()[i], circle_x, circle_y))
            vwc.add(Triple(g.getVertices()[i], circle_x + 25.0, circle_y + 25.0))
        }
        var numberEdge = 0
        for (a in 0 until g.getMatrix().size){
            for (b in 0 until g.getMatrix()[a].size){
                // println(g.getMatrix()[a][b])
                if (g.getMatrix()[a][b] > 0) {
                    numberEdge++
                    val startX = vwc[a].second
                    val startY = vwc[a].third

                    val line = Line(vwc[a].second, vwc[a].third, vwc[b].second, vwc[b].third)
                    var edge = shortenedLine(line, 25.0)
                    val endX = edge.endX
                    val endY = edge.endY
                    var centerX = (edge.endX + edge.startX) / 2
                    var centerY = (edge.endY + edge.startY) / 2
                    val dist = 45.0
                    if (g.getMatrix()[b][a] > 0) {
                        val mul = if (b > a ) -1 else 1
                        var curvePair = returnPoint(startX, endX, startY, endY, dist, mul)
                        centerX = curvePair.first
                        centerY = curvePair.second
                        val curve = QuadCurve(
                            startX,
                            startY,
                            centerX,
                            centerY,
                            endX,
                            endY
                        )
                        curvePair = returnPoint(startX, endX, startY, endY, dist / 2, mul)
                        centerX = curvePair.first
                        centerY = curvePair.second
                        curve.fill = Color.TRANSPARENT
                        curve.stroke = Color.BLACK
                        p.add(curve)
                        p.add(Arrow(curvePair.first, curvePair.second, curve.endX, curve.endY, 10.0, true))
                    }
                    else
                        p.add(Arrow(edge.startX, edge.startY, edge.endX, edge.endY, 10.0))

                    val weight = Label(g.getMatrix()[a][b].toString())
                    weight.setStyle("-fx-font-smoothing-type: lcd; -fx-fill: white; -fx-font-size: 15pt;")
                    weight.layoutX = centerX
                    weight.layoutY = centerY
                    p.add(weight)
                }
            }
        }
        for (e in vwc){
            p.add(paintVertex(e.first, e.second - 25.0, e.third - 25.0))
        }
        // p.setStyle("-fx-border-color: black");
        return p
    }
}
