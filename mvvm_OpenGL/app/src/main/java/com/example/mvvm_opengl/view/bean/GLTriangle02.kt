package com.example.mvvm_opengl.view.bean

import android.opengl.GLES20
import com.example.mvvm_opengl.view.utils.GLUtil.floatArray2FloatBuffer
import com.example.mvvm_opengl.view.utils.GLUtil.loadShader
import java.nio.FloatBuffer


class GLTriangle02 {

    // 顶点着色器的脚本
    var vertexShaderCode = "uniform mat4 uMVPMatrix;" +  //接收传入的转换矩阵
            " attribute vec4 vPosition;" +  //接收传入的顶点
            " void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +  //矩阵变换计算之后的位置
            " }"

    // 片元着色器的脚本
    var fragmentShaderCode =
        " precision mediump float;" +  // 声明float类型的精度为中等(精度越高越耗资源)
                " uniform vec4 vColor;" +  // 接收传入的颜色
                " void main() {" +
                "     gl_FragColor = vColor;" +  // 给此片元的填充色
                " }"

    private var vertexBuffer //顶点坐标数据要转化成FloatBuffer格式
            : FloatBuffer? = null


    // 数组中每3个值作为一个坐标点
    val COORDS_PER_VERTEX = 3

    //三角形的坐标数组
    var triangleCoords = floatArrayOf(
        0.0f, 0.5f, 0.0f,  // top
        -0.5f, -0.5f, 0.0f,  // bottom left
        0.5f, -0.5f, 0.0f // bottom right
    )

    //顶点个数，计算得出
    private val vertexCount = triangleCoords.size / COORDS_PER_VERTEX

    //一个顶点有3个float，一个float是4个字节，所以一个顶点要12字节
    private val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per mVertex


    //三角形的颜色数组，rgba
    private val mColor = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f
    )

    //当前绘制的顶点位置句柄
    private var vPositionHandle = 0

    //片元着色器颜色句柄
    private var vColorHandle = 0

    //变换矩阵句柄
    private var mMVPMatrixHandle = 0

    //这个可以理解为一个OpenGL程序句柄
    private var mProgram = 0

    //变换矩阵，提供set方法
    private var mvpMatrix = FloatArray(16)
    fun setMvpMatrix(mvpMatrix: FloatArray) {
        this.mvpMatrix = mvpMatrix
    }


    init {
        /** 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理 */
        vertexBuffer = floatArray2FloatBuffer(triangleCoords)
        /** 2、加载编译顶点着色器和片元着色器 */
        val vertexShader = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader = loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )
        /** 3、创建空的OpenGL ES程序，并把着色器添加进去 */
        mProgram = GLES20.glCreateProgram()

        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, vertexShader)

        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader)
        /** 4、链接程序 */
        GLES20.glLinkProgram(mProgram)
    }


    fun draw() {

        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram)
        /***1.获取句柄 */
        // 获取顶点着色器的位置的句柄（这里可以理解为当前绘制的顶点位置）
        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        // 获取片段着色器的vColor句柄
        vColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        // 获取变换矩阵的句柄
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        /**2.设置数据 */
        // 启用顶点属性，最后对应禁用
        GLES20.glEnableVertexAttribArray(vPositionHandle)

        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(
            vPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )
        // 设置绘制三角形的颜色，给vColor 这个变量赋值
        GLES20.glUniform4fv(vColorHandle, 1, mColor, 0)
        // 将投影和视图转换传递给着色器，可以理解为给uMVPMatrix这个变量赋值为mvpMatrix
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0)
        /** 3.绘制三角形，三个顶点 */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // 禁用顶点数组（好像不禁用也没啥问题）
        GLES20.glDisableVertexAttribArray(vPositionHandle)
    }
}