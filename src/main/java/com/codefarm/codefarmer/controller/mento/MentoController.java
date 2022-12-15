package com.codefarm.codefarmer.controller.mento;


import com.codefarm.codefarmer.domain.chat.ChatDTO;
import com.codefarm.codefarmer.domain.mentor.MentorBoardDTO;
import com.codefarm.codefarmer.domain.mentor.MentorDTO;
import com.codefarm.codefarmer.entity.chat.Chat;
import com.codefarm.codefarmer.repository.chat.ChatRepository;
import com.codefarm.codefarmer.repository.mentor.MentorRepository;
import com.codefarm.codefarmer.service.chat.ChatRoomService;
import com.codefarm.codefarmer.service.mentor.MentorService;
import com.codefarm.codefarmer.type.MemberType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.xml.ws.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/mento/*")
public class MentoController {
    private final ChatRoomService cs;
    private final MentorService ms;
    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate template;
    private final MentorService mentorService;
    private final MentorRepository mentorRepository;

    public MentoController(ChatRoomService cs, MentorService ms, ChatRepository chatRepository, SimpMessagingTemplate template, MentorService mentorService, MentorRepository mentorRepository) {
        this.cs = cs;
        this.ms = ms;
        this.chatRepository = chatRepository;
        this.template = template;
        this.mentorService = mentorService;
        this.mentorRepository = mentorRepository;
    }


    @GetMapping("/intro")
    public void mentoIntro(Model model) {
      model.addAttribute("lists",mentorService.getIntroMentor());

    }

    @GetMapping("/list")
    public void list(Model model, HttpSession session){
        Long memberId = (Long)session.getAttribute("memberId");
        Object memberType = (Object)session.getAttribute("memberType");
        model.addAttribute("sessionMemberType", memberType);
        model.addAttribute("sessionMemberId", memberId);
    }

    @GetMapping("/write")
    public void write(Model model, HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        model.addAttribute("mentorBoard", new MentorBoardDTO());
    }

    @PostMapping("/write")
    public RedirectView writeFin(MentorBoardDTO mentorBoardDTO, RedirectAttributes redirectAttributes, HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        mentorBoardDTO.setMemberId(sessionId);
        mentorBoardDTO.setMentorId(mentorService.findByMemberId(sessionId));
        mentorService.mentorBoardAdd(mentorBoardDTO);
        redirectAttributes.addFlashAttribute("mentorBoardId", mentorBoardDTO.getMentorBoardId());

        return new RedirectView("/mento/list");
    }
    @GetMapping("/detail")
    public void detail(Model model,Long mentorBoardId ,HttpSession session){
        /*선택한 게시글의 번호로 글의 정보를 program JSON 형식으로 전송(이후에 boardNumber에는 클릭한 값을 받아서 작업 필요)*/
        model.addAttribute("mentorId", ms.showDetailMentorBoard(mentorBoardId).getMentorId());
//        MentorBoardDTO info = mentorService.getInfo();
//      멘토 상세페이지 보여주기
        MentorBoardDTO list = mentorService.showDetailMentorBoard(mentorBoardId);
        Long writeMemberId = mentorService.findmentorByBoardId(mentorBoardId);
        Long mentorId = mentorService.findmentoInfo(writeMemberId);
        MentorDTO mentorDTO = mentorService.findBymentoId(mentorId);
        /*로그인 세션 변수로 보내기*/
        Long sessionId = (Long) session.getAttribute("memberId");
//        Long sessionId = 1L;
        model.addAttribute("mentorDTO" , mentorDTO);
        model.addAttribute("sessionId", sessionId);
//        멘토 상세
        model.addAttribute("list", list);
    }


//    멘토 수정 페이지 이동
    @GetMapping("/update")
    public void update(Model model, @RequestParam Long mentorBoardId){
        MentorBoardDTO updateRegister = mentorService.showUpdate(mentorBoardId);
        model.addAttribute("updateRegister", updateRegister);
    }

//    멘토 수정하고 제출버튼 클릭 시
    @PostMapping("/update")
    public RedirectView updateFin(@RequestParam Long mentorBoardId,@RequestParam Long mentorId ,MentorBoardDTO mentorBoardDTO, HttpSession session){
        Long sessionId = (Long)session.getAttribute("memberId");
        log.info("dto : " + mentorBoardDTO);
//        세션에 memberId넣기
        mentorBoardDTO.setMemberId(sessionId);
        mentorBoardDTO.setMentorId(mentorId);

        mentorBoardDTO.setMentorBoardId(mentorBoardId);
        mentorService.update(mentorBoardDTO);

        return new RedirectView("list");
    }

    @GetMapping("/delete")
    public RedirectView delete(@RequestParam Long mentorBoardId){

        mentorService.removeMentorBoard(mentorBoardId);
        return new RedirectView("/mento/list");
    }




    /*채팅방 이동 시*/
    @GetMapping("/chatting")
    @RequestMapping(value = "/mento/chatting", method = RequestMethod.GET)
    public void chatting(Model model, Long mentorId, HttpSession session) {
        /*로그인 세션 변수로 보내기*/
        Long sessionId = (Long) session.getAttribute("memberId");
//        Long sessionId = 1L;
        model.addAttribute("sessionId", sessionId);
        mentorId = 86L;

        /*대화가 이미 있는지에 따라 채팅방 생성*/
        cs.createChatRoom(mentorId, sessionId); // 게시글을 작성한 멘토 멤버아이디와 로그인 세션
        /*로그인 멤버 세션이 참여 중인 대화방 목록 저장*/
        model.addAttribute("rooms", cs.chatRoomSelectAll(sessionId));
        /*로그인 세션에 따른 읽지 않은 메세지 개수 가져오기*/
        model.addAttribute("alarmCnt", cs.chatAlarm(sessionId)); // 메세지 개수의 경우 이후 세션으로 등록 필요(모든 페이지에서 쓰기 위함)
    }
}
















