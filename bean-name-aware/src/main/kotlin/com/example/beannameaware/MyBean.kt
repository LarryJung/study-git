package com.example.beannameaware

import org.springframework.beans.factory.BeanNameAware
import org.springframework.stereotype.Component

/**
 * @author Larry
 */
@Component
class CustomBean1 : BeanNameAware {
    override fun setBeanName(name: String) {
        println("bean name: $name") // customBean1
    }
}

@Component("cuscusbean")
class CustomBean2 : BeanNameAware {
    override fun setBeanName(name: String) {
        println("bean name: $name") // cuscusbean
    }
}
