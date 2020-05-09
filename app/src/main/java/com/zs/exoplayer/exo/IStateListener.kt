package com.fpc.com.exo

/**
 * @description
 * @author Created by fpc on 2019-08-13 10:08.
 */
interface IStateListener {
    fun onStart(){}
    fun onPause(){}
    fun onBuffering(){}
    fun onEnd(){}
    fun onIdle(){}
}