package com.codefarm.codefarmer.controller.join;

import com.codefarm.codefarmer.domain.member.MemberDTO;
import com.codefarm.codefarmer.service.join.JoinKakaoService;
import com.codefarm.codefarmer.service.join.JoinService;
import com.codefarm.codefarmer.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@Slf4j
@RequestMapping("/register/*")
@RequiredArgsConstructor
public class JoinController {

    private final JoinKakaoService joinKakaoService;
    private final JoinService joinService;
    private final MemberService memberService;

    @GetMapping("")
    public String joinPage(){
        return "/join/join";
    }

    @ResponseBody
    @GetMapping("/kakao")
    public RedirectView kakaoLogin(String code, RedirectAttributes redirectAttributes){
        log.info("코드 : "+code);
        String token = joinKakaoService.getKakaoAccessToken(code);

        try {
            String email = joinKakaoService.getKakaoInfo(token);
            redirectAttributes.addFlashAttribute("memberEmail",email);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new RedirectView("/register/form");
    }

    @GetMapping("/form")
    public String joinFormPage(MemberDTO memberDTO){
        return "/join/joinForm";
    }

    @GetMapping("/agreement")
    public String agreementPage(){
        return "/join/agreement";
    }

    @ResponseBody
    @PostMapping("/form")
    public Integer checkUserNick(@RequestParam("memberNickname") String nickname){
        if(joinService.checkUserNick(nickname) == 1){
            log.info("???");
            return 1;
        }else {
            log.info("!!!");
            return 0;
        }
    }


    @PostMapping("/joinOk")
    public String joinOk(MemberDTO memberDTO){
        memberService.join(memberDTO);
        return "/main/main";
    }


}
