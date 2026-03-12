package com.spring.app.board.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.auth.domain.CustomUserDetails;
import com.spring.app.board.domain.BoardDTO;
import com.spring.app.board.service.BoardService;
import com.spring.app.entity.Board;
import com.spring.app.entity.Member;
import com.spring.app.member.repository.MemberRepository;
import com.spring.app.util.MyUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor  // @RequiredArgsConstructor는 Lombok 라이브러리에서 제공하는 애너테이션으로, final 필드 또는 @NonNull이 붙은 필드에 대해 생성자를 자동으로 생성해준다. 
@RequestMapping("/board/") 
public class BoardController {

 // === 생성자 주입 (Constructor Injection) ===
 	private final BoardService boardService;  // Query DSL(Domain Specific Language) 을 사용한 것
	
 	private final MemberRepository memberRepository;
 	
	// 목록보기
	@GetMapping("list")
	public String list(@RequestParam(name="searchType", defaultValue="")  String searchType,
			           @RequestParam(name="searchWord", defaultValue="")  String searchWord,
			           @RequestParam(value="pageno",    defaultValue="1") int currentShowPageNo, /* 현재 페이지번호 */   
			           Model model,
			           HttpServletRequest request, HttpServletResponse response) {
		
		int sizePerPage = 5;      // 한 페이자당 보여질 행의 개수.
		                          // (DB 가 Oracle 12C 이상인 경우
	                              //  sizePerPage 가 FETCH NEXT 행개수 의 역할을 함.
								  //	 SELECT * 
								  //	 FROM employees 
								  //     ORDER BY employee_id 
								  //     OFFSET 20 ROWS 
		                          //     FETCH NEXT 10 ROWS ONLY;
		
		int totalPage = 0;        // 전체 페이지 개수
		long totalDataCount = 0;  // 전체 데이터의 개수
		String pageBar = "";      // 페이지바
				
		try {
			Page<Board> pageBoard = boardService.getPageBoard(searchType, searchWord, currentShowPageNo, sizePerPage); 
            /* Page 는 페이징 처리된 데이터와 함께, 
			   페이지 정보(총 페이지수, 전체 아이템 개수, 현재 페이지 번호, 현재 페이지의 아이템 등등)를 
			   제공해주는 인터페이스이다.

			   !!! Page 객체는 일반적으로 Repository의 조회 메소드(findAll(), findBy필드명, 등등)에서 Pageable 객체를 파라미터로 받아서 결과물을 반환할 때 사용된다. !!! 
			*/
			
			/* >> Page 객체의 주요 메소드들 <<

				getContent()
				→ 현재 페이지의 데이터 목록을 반환한다.
				
				getTotalElements()
				→ 쿼리 결과물의 전체 데이터 개수이다. 
				  즉, Pageable에 의해 limit 조건(offset 행개수 row fetch next 행개수 row only)이 들어가지 않는 쿼리 결과의 수 인데, 
				  주의해야 할 점은 쿼리 결과의 갯수만 가져오는 것이지 전체 데이터를 가져오지 않는다는 점이다.
				  이 메소드는 게시판 기능에서 사용자에게 전체 데이터 개수를 알려주는 등에 사용하기 좋다.
				  [ limit(offset 행개수 row fetch next 행개수 row only) 조건은 조회할 데이터의 최대 개수를 설정하는 조건으로, 페이징 처리에서 중요한 역할을 한다. 
				    limit(offset 행개수 row fetch next 행개수 row only) 조건은 데이터베이스에서 조회할 결과의 최대 개수를 제한하는 역할을 한다.
				    
				    Spring Data JPA의 Pageable을 사용할 때 각 페이지별 보여질 목록의 개수인 sizePerPage 만 알려주면 
				    limit 조건과 offset 조건이(offset 행개수 row fetch next 행개수 row only 가) 자동으로 적용된다.
				  ] 

				
				getTotalPages()
				→ 쿼리를 통해 가져온 요소들을 size크기(sizePerPage)에 맞춰 페이징하였을 때 나오는 총 페이지의 갯수이다.
				  이를 활용해 쉽게 페이지 버튼의 생성이 가능하다.
				
				getSize()
				→ 쿼리를 수행한 전체 데이터에 대해 일정 수 만큼 나눠 페이지를 구성하는데, 이 일정 수의 크기이다.
				
				getNumber()
				→ 요소를 가져온 페이지의 번호(현재 페이지 번호)를 의미한다.
				
				getNumberOfElements()
				→ 페이지에 존재하는 요소의 개수이다. 최대 size의 수 만큼 나올 수 있다.
 
			*/
			
			totalPage = pageBoard.getTotalPages(); // 전체 페이지 개수
			
			if (currentShowPageNo > totalPage) {
				currentShowPageNo = totalPage;
				pageBoard = boardService.getPageBoard(searchType, searchWord, currentShowPageNo, sizePerPage); 
			}
			
			totalDataCount = pageBoard.getTotalElements(); // 전체 데이터의 개수
			
			List<Board> boardList = pageBoard.getContent(); // 현재 페이지의 데이터 목록을 반환한다.
			
			// 현재 페이지의 데이터 목록인 List<Board> 를 List<BoardDTO> 로 변환한다.
			List<BoardDTO> boardDtoList = boardList.stream() 
					                               .map(entity -> BoardDTO.builder()
					                            		   .num(entity.getNum())
					                            		   .memberid(entity.getMember().getMemberid())
					                            		   .name(entity.getMember().getName())
					                            		   .subject(entity.getSubject())
					                            		   .regDate(entity.getRegDate())
					                            		   .readCount(entity.getReadCount())
					                            		   .build())
					                               .toList(); // Java 16 이상인 경우임. 만약에 Java 8 ~ 15라면 .toList(); 가 아니라 .collect(Collectors.toList()); 임.
			
			model.addAttribute("boardDtoList", boardDtoList);
			
			if(!"".equals(searchType) && !"".equals(searchWord)) {
				model.addAttribute("searchType", searchType); // view단페이지에서 검색타입 유지
				model.addAttribute("searchWord", searchWord); // view단페이지에서 검색어 유지
			}
			
			///////////////////////////////////////////////////////////////////////
			
			// ========= 페이지바 만들기 시작 ========= //
			int blockSize = 10;
			// blockSize 는 1개 블럭(토막)당 보여지는 페이지번호의 개수이다.
			/*
				             1  2  3  4  5  6  7  8  9 10 [다음][마지막]  -- 1개블럭
				[맨처음][이전]  11 12 13 14 15 16 17 18 19 20 [다음][마지막]  -- 1개블럭
				[맨처음][이전]  21 22 23
			*/
			
			int loop = 1;
			/*
		    	loop는 1부터 증가하여 1개 블럭을 이루는 페이지번호의 개수[ 지금은 10개(== blockSize) ] 까지만 증가하는 용도이다.
		    */
			
			int pageno = ((currentShowPageNo - 1)/blockSize) * blockSize + 1;
			// *** !! 공식이다. !! *** //
			
		/*
		    1  2  3  4  5  6  7  8  9  10  -- 첫번째 블럭의 페이지번호 시작값(pageno)은 1 이다.
		    11 12 13 14 15 16 17 18 19 20  -- 두번째 블럭의 페이지번호 시작값(pageno)은 11 이다.
		    21 22 23 24 25 26 27 28 29 30  -- 세번째 블럭의 페이지번호 시작값(pageno)은 21 이다.
		    
		    currentShowPageNo         pageno
		   ----------------------------------
		         1                      1 = ((1 - 1)/10) * 10 + 1
		         2                      1 = ((2 - 1)/10) * 10 + 1
		         3                      1 = ((3 - 1)/10) * 10 + 1
		         4                      1
		         5                      1
		         6                      1
		         7                      1 
		         8                      1
		         9                      1
		         10                     1 = ((10 - 1)/10) * 10 + 1
		        
		         11                    11 = ((11 - 1)/10) * 10 + 1
		         12                    11 = ((12 - 1)/10) * 10 + 1
		         13                    11 = ((13 - 1)/10) * 10 + 1
		         14                    11
		         15                    11
		         16                    11
		         17                    11
		         18                    11 
		         19                    11 
		         20                    11 = ((20 - 1)/10) * 10 + 1
		         
		         21                    21 = ((21 - 1)/10) * 10 + 1
		         22                    21 = ((22 - 1)/10) * 10 + 1
		         23                    21 = ((23 - 1)/10) * 10 + 1
		         ..                    ..
		         29                    21
		         30                    21 = ((30 - 1)/10) * 10 + 1
		*/
			
			pageBar = "<ul style='list-style:none;'>";
			String url = "/board-service/board/list";
			
			// === [맨처음][이전] 만들기 === //
			if(pageno != 1) {
				pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&pageno=1'>[맨처음]</a></li>";
				pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&pageno="+(pageno-1)+"'>[이전]</a></li>"; 
			}
			
			while( !(loop > blockSize || pageno > totalPage) ) {
				
				if(pageno == currentShowPageNo) {
					pageBar += "<li style='display:inline-block; width:30px; font-size:12pt; border:solid 1px gray; color:red; padding:2px 4px;'>"+pageno+"</li>";
				}
				else {
					pageBar += "<li style='display:inline-block; width:30px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&pageno="+pageno+"'>"+pageno+"</a></li>"; 
				}
				
				loop++;
				pageno++;
			}// end of while------------------------
			
			// === [다음][마지막] 만들기 === //
			if(pageno <= totalPage) {
				pageBar += "<li style='display:inline-block; width:50px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&pageno="+pageno+"'>[다음]</a></li>";
				pageBar += "<li style='display:inline-block; width:70px; font-size:12pt;'><a href='"+url+"?searchType="+searchType+"&searchWord="+searchWord+"&pageno="+totalPage+"'>[마지막]</a></li>"; 
			}
			
			pageBar += "</ul>";
			
			model.addAttribute("pageBar", pageBar);
			
			// ========= 페이지바 만들기 끝 ========= //
			
			model.addAttribute("totalDataCount", totalDataCount); // 페이징 처리시 보여주는 순번을 나타내기 위한 것임. 
			model.addAttribute("currentShowPageNo", currentShowPageNo); // 페이징 처리시 보여주는 순번을 나타내기 위한 것임.
			model.addAttribute("sizePerPage", sizePerPage); // 페이징 처리시 보여주는 순번을 나타내기 위한 것임.
			
			// 페이징 처리되어진 후 특정 글제목을 클릭하여 상세내용을 본 이후
			// 사용자가 "검색된결과목록보기" 버튼을 클릭했을때 돌아갈 페이지를 알려주기 위해 현재 페이지 URL 주소를 쿠키에 저장한다.
			String listURL = MyUtil.getCurrentURL(request);
		/*	
			Cookie cookie = new Cookie("listURL", listURL); 
	        // new Cookie(쿠키명, 쿠키값); 
			// Cookie 클래스 임포트시 jakarta.servlet.http.Cookie 임.

			cookie.setMaxAge(24*60*60); // 쿠키수명은 1일로 함
			cookie.setPath("/board/");  // 쿠키가 브라우저에서 전송될 URL 경로 범위(Path)를 지정하는 설정임
			 
			//   Path를 /board/ 로 설정하면:
	        //   /board/view_2, /board/view 등 /board/ 로 시작하는 경로에서만 쿠키가 전송된다.
	        //   /, /index, /login 등의 다른 경로에서는 이 쿠키는 사용되지 않음.   
	        
	        response.addCookie(cookie); // 쿠키에 저장. 접속한 클라이언트 PC로 쿠키를 보내줌
		 */
			ResponseCookie cookie = ResponseCookie.from(
	                "listURL", // 쿠키명
	                 listURL   // 쿠키밸류값은 현재 페이지 URL 주소 
	        )
	                .secure(false)  // localhost HTTP 이므로 false   https 이라면 .secure(true)
	                .path("/board-service/")      // 쿠키가 사용되어질 범위 
	                .maxAge(60 * 60 * 24 * 1)  // 쿠키수명 1일 
	                .sameSite("Strict")   // 단일 도메인인 경우 .sameSite("Strict") , React 를 사용하여 프론트(localhost:3000)/백엔드(localhost:9082) 로 분리된 경우라면 .sameSite("None")  
	                .build();

	        // 생성된 쿠키 전송하기 
	        response.addHeader("Set-Cookie", cookie.toString());
		 
		} catch(Exception e) {
			// e.printStackTrace();
		}
		
		return "board/list";
	}
	
	
	// 글쓰기 폼 요청
	@PreAuthorize("isAuthenticated()")
	@GetMapping("write")
	public String writeForm() {
		
		return "board/write";
	}
	
	
	// 글쓰기 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("write")
	@ResponseBody
	public Map<String, Integer> writeSubmit(BoardDTO boardDto) {
        
		System.out.println("=== 확인용 시작 ===");
		System.out.println("memberid : " + boardDto.getMemberid());
		System.out.println("글제목 : " + boardDto.getSubject());
		System.out.println("글내용 : " + boardDto.getContent());
		System.out.println("=== 확인용 끝 ===");
		
		Map<String, Integer> map = new HashMap<>();
		
		try {
			Member member = memberRepository.findById(boardDto.getMemberid())
	                 .orElseThrow(() -> new RuntimeException("회원 정보가 없습니다"));
			
			Board entity = Board.builder()
					      .member(member)
					      .subject(boardDto.getSubject())
					      .content(boardDto.getContent())
					      .build();
			
			boardService.insertBoard(entity);
			map.put("n", 1);
		} catch (Exception e) {
			e.printStackTrace();
			map.put("n", 0);
		}

		return map;
	}
	
	
	// 글 상세 보기
	@GetMapping("view")
	public String view(@RequestParam Long num,
			           @RequestParam(name="memberid",   defaultValue = "") String memberid, 
			           @RequestParam(name="searchType", defaultValue = "") String searchType,
	                   @RequestParam(name="searchWord",defaultValue = "") String searchWord,
			           HttpServletRequest request) {

		try {
			
			BoardDTO boardDto = boardService.viewBoard(num);
			
			// 로그인해서 다른 사용자가 작성한 글을 조회할 경우 
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("memberid : " + memberid);
			System.out.println("boardDto.getMemberid() : " + boardDto.getMemberid());
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			
			if( memberid != "" &&
				!memberid.equals(boardDto.getMemberid()) ) {
				boardService.updateReadCount(num); // 조회수 증가하기
			}
			
			boardDto = boardService.viewBoard(num); // 조회수 증가한 경우 또는 조회수 증가하지 않은 경우 가져오기 
			
			request.setAttribute("boardDto", boardDto);
			
			// === 이전 글(미래) === //
			BoardDTO preBoardDto = boardService.getPreBoardDto(num, searchType, searchWord);
			request.setAttribute("preBoardDto", preBoardDto);
			
			// === 다음 글(과거) === //
			BoardDTO nextBoardDto = boardService.getNextBoardDto(num, searchType, searchWord);
			request.setAttribute("nextBoardDto", nextBoardDto);
			
			request.setAttribute("searchType", searchType);
			request.setAttribute("searchWord", searchWord);
			// === 이전 글(미래), 다음 글(과거) 끝 === //
			
			
		} catch(IllegalArgumentException e) {
			// 게시글이 없을 경우
			
			String message = "해당 게시글이 존재하지 않습니다.";
		 	String loc = request.getContextPath()+"/board/list";
		 	   	   
		 	request.setAttribute("message", message);
		 	request.setAttribute("loc", loc);
		 	return "msg";
		 	
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return "board/view";
	}
	
	
	// 글수정 폼 요청
	@PreAuthorize("isAuthenticated()")
	@GetMapping("update")
	public String updateForm(@RequestParam(name="num") Long num
			               , HttpServletRequest request
			               , @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		try {
			BoardDTO boardDto = boardService.viewBoard(num);
		 
			String loginMemberId = null;
			
			if (userDetails != null) {
				loginMemberId = userDetails.getUsername();
			}
			
			if(boardDto != null && !boardDto.getMemberid().equals(loginMemberId)) { // 다른 사람의 게시글인 경우
				String message = "다른 사용자의 글은 수정이 불가합니다.";
			 	String loc = request.getContextPath()+"/board/list";
			 	   	   
			 	request.setAttribute("message", message);
			 	request.setAttribute("loc", loc);
			 	return "msg";
			}
			else {
				request.setAttribute("boardDto", boardDto);
			}
			
		} catch(Exception e) {
			// 게시글이 없을 경우
			
			e.printStackTrace();
			return "redirect:/board/list";
		}
		
		return "board/update";
		
	}

	
	// 글수정 완료
	@PreAuthorize("isAuthenticated()")
	@PostMapping("update")
	@ResponseBody
	public Map<String, Long> updateSubmit(BoardDTO dto) {
		
		Map<String, Long> map = new HashMap<>();
		
		try {
			 Board entity = Board.builder()
			                     .num(dto.getNum())
			                     .subject(dto.getSubject())
			                     .content(dto.getContent())
			                     .build();
			   
			 boardService.updateBoard(entity);
			 
			 map.put("n", dto.getNum());

		} catch (Exception e) {
			e.printStackTrace();
			map.put("n", 0L);
		}

		return map;
	}

	
	// 글삭제 완료
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("delete")
	@ResponseBody
	public Map<String, Integer> delete(@RequestParam(name="num") Long num,
			                           @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		Map<String, Integer> map = new HashMap<>();
		
		try {
		    BoardDTO boardDto = boardService.viewBoard(num);
		    		    
            String loginMemberId = null;
			
			if (userDetails != null) {
				loginMemberId = userDetails.getUsername(); // 로그인 되어진 사용자 아이디
			}
			
			if(boardDto != null && !boardDto.getMemberid().equals(loginMemberId)) { 
				                // 다른 사용자가 쓴 게시글인 경우
				map.put("n", 2);
			}
			else { 
				// 자신이 쓴 게시글인 경우 
				boardService.deleteBoard(num);
				map.put("n", 1);
			}
		 	
		} catch(Exception e) {
			// 게시글이 없을 경우
			e.printStackTrace();
			map.put("n", 0);
		}
		
		return map;
		
	}
	
}
