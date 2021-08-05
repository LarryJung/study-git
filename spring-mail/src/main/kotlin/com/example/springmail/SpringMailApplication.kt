package com.example.springmail

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import javax.mail.internet.MimeMessage

@SpringBootApplication
class SpringMailApplication {

    @Bean
    fun init(mailSender: JavaMailSender) = CommandLineRunner {
        val preparator = MimeMessagePreparator { message: MimeMessage? ->
            val helper = MimeMessageHelper(message!!)
            helper.setTo("blabla email")
            helper.setSubject("제목입니당.")
            helper.setText("내용입니당.")
        }
        mailSender.send(preparator)
    }
}

fun main(args: Array<String>) {
    runApplication<SpringMailApplication>(*args)
}

