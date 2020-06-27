package me.nuguri.auth.controller.view;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.common.entity.QAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Transactional
    @RequestMapping({"/main", "/"})
    public String main() {
        jpaQueryFactory.selectFrom(QAccount.account)
                .where(QAccount.account.email.eq("admin@naver.com"))
                .fetchOne();
        return "main";
    }

}
