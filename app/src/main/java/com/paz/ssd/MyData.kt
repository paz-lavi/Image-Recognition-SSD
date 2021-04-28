package com.paz.ssd

data class MyData(
        val name : String?,
        val index : Int,
        val result : Float,
        val per : Float
) : Comparable<MyData>{
    override fun compareTo(other: MyData): Int {
       return when {
            (result - other.result) > 0f -> return 1
            (result - other.result) < 0f -> return  -1
            else -> 0
        }
    }

    override fun toString(): String {
       return  if (name != null){
           "$name: $per%"
       }else{
           ""
       }
    }
}
