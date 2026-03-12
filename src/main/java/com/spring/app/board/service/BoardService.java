package com.spring.app.board.service;

import org.springframework.data.domain.Page;

import com.spring.app.board.domain.BoardDTO;
import com.spring.app.entity.Board;

public interface BoardService {

	public void insertBoard(Board entity) throws Exception;
		
	public Page<Board> getPageBoard(String searchType, String searchWord, int currentShowPageNo, int sizePerPage); 
	
	public BoardDTO viewBoard(long num) throws Exception;
	
	public void updateBoard(Board entity) throws Exception;
	
	public void deleteBoard(long num) throws Exception;
	
	public void updateReadCount(long num) throws Exception;

	// 이전 글(미래)
	public BoardDTO getPreBoardDto(long num, String searchType, String searchWord);

	// 다음 글(과거)
	public BoardDTO getNextBoardDto(long num, String searchType, String searchWord);
	
}
