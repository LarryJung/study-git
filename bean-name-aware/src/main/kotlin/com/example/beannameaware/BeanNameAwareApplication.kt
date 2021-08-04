package com.example.beannameaware

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BeanNameAwareApplication

fun main(args: Array<String>) {
	runApplication<BeanNameAwareApplication>(*args)
}
