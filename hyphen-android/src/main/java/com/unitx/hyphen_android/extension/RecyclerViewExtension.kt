package com.unitx.hyphen_android.extension

import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay

suspend inline fun <reified A : RecyclerView.Adapter<*>> RecyclerView.adaptUpWithDelay(
    adapter: A,
    layoutManager: RecyclerView.LayoutManager,
    duration: Long,
    crossinline beforeAttach: A.() -> Unit = {},
    crossinline afterAttach: A.() -> Unit = {},
): A {
    if (this.adapter == null) {
        delay(duration)
        this.layoutManager = layoutManager
        this.adapter = adapter
        (this.adapter as A).beforeAttach()
    }
    (this.adapter as A).afterAttach()
    return this.adapter as A
}

inline fun <reified A : RecyclerView.Adapter<*>> RecyclerView.adaptUp(
    adapter: A,
    layoutManager: RecyclerView.LayoutManager,
    crossinline noAttach: A.() -> Unit = {},
    crossinline beforeAttach: A.() -> Unit = {},
    crossinline afterAttach: A.() -> Unit = {},
): A {
    if (this.adapter == null) {
        this.layoutManager = layoutManager
        this.adapter = adapter
        (this.adapter as A).beforeAttach()
    } else (this.adapter as A).noAttach()
    (this.adapter as A).afterAttach()
    return this.adapter as A
}

inline fun <reified A : RecyclerView.Adapter<*>> RecyclerView.onGlobalLayoutAdapter(
    crossinline onListenerApplied: A.() -> Unit
): ViewTreeObserver.OnGlobalLayoutListener {
    val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            val adapter = this@onGlobalLayoutAdapter.adapter
            if (adapter is A) {
                adapter.onListenerApplied()
            } else {
                error("Adapter is not of type ${A::class.java.simpleName}")
            }
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(listener)
    return listener
}


//private fun onGlobalLayoutListener(
//    rv: RecyclerView = binding.hfRvSelectMustHaves,
//    onListenerApplied: (adapter: MustHaveModelAdapter) -> Unit
//): ViewTreeObserver.OnGlobalLayoutListener {
//    return object : ViewTreeObserver.OnGlobalLayoutListener {
//        override fun onGlobalLayout() {
//            rv.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            val rvWidth = rv.width
//            (rv.adapter as MustHaveModelAdapter).apply {
//                itemWidth = rvWidth / SCREEN_ITEM_COUNT
//                onListenerApplied(this)
//            }
//        }
//    }
//}


