package com.example.model

import com.example.utils.Ddot
import org.apache.spark.ml.linalg.{DenseVector, Vector, Vectors}
import org.apache.spark.rdd.RDD

class FTRL(W: Array[Double]) extends Serializable {

  val L1 = 1
  val L2 = 1
  val alpha = 0.1
  val belta = 1

  var N: Array[Double] = Array(W.length)
  var Z: Array[Double] = Array(W.length)


  def dot(x: DenseVector, y: DenseVector): Double = {
    val n = x.size
    Ddot.ddot(n, x.values, 0, 1, y.values, 0, 1)
  }

  def fit(data: RDD[(Int, Vector)], label: Double) : Unit = {
    var p = 0.0
    data.map{
      case (i, v) =>
        val sgn = if (Z(i) < 0 ) -1.0 else 1.0
        if (sgn * Z(i) <= L1) {
          W(i) = 0.0
        } else {
          W(i) = (sgn * L1 - Z(i)) /
            ((belta + math.sqrt(N(i))) / alpha + L2)
        }

        p = dot(v.toDense, Vectors.dense(W(i)).toDense)
    }

    val mult = label * (1 / (1 + Math.exp(-p * label)) - 1)

    data.map {
      case (i,v) =>
        val g = dot(Vectors.dense(mult).toDense, v.toDense)
        val sigma = (math.sqrt(N(i) + g * g) - math.sqrt(N(i))) / alpha
        Z(i) = Z(i) + g - sigma * W(i)
        N(i) = N(i) + g * g
    }

  }

  def predict(data: Vector): Double ={
    var p = 0.0
    p = dot(data.toDense, Vectors.dense(W).toDense)
    p = 1.0 / (1.0 + math.exp(-p))

    p
  }

  def getW: Array[Double] = {
    W
  }

}

case object FTRL {
  def apply(W: Array[Double]): FTRL = new FTRL(W)
}



