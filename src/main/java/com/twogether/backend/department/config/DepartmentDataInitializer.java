package com.twogether.backend.department.config;

import com.twogether.backend.department.domain.Campus;
import com.twogether.backend.department.domain.College;
import com.twogether.backend.department.domain.Department;
import com.twogether.backend.department.repository.CollegeRepository;
import com.twogether.backend.department.repository.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DepartmentDataInitializer
        implements CommandLineRunner {

    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;

    public DepartmentDataInitializer(
            CollegeRepository collegeRepository,
            DepartmentRepository departmentRepository
    ) {
        this.collegeRepository = collegeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (collegeRepository.count() > 0) {
            return;
        }

        College semiconductorIctCollege =
                collegeRepository.save(
                        new College(
                                "반도체ICT대학",
                                Campus.NATURAL
                        )
                );

        College engineeringCollege =
                collegeRepository.save(
                        new College(
                                "공과대학",
                                Campus.NATURAL
                        )
                );

        College humanitiesCollege =
                collegeRepository.save(
                        new College(
                                "인문대학",
                                Campus.HUMANITIES
                        )
                );

        College businessCollege =
                collegeRepository.save(
                        new College(
                                "경영대학",
                                Campus.HUMANITIES
                        )
                );

        departmentRepository.saveAll(
                List.of(

                        new Department("컴퓨터공학전공", semiconductorIctCollege),
                        new Department("정보통신공학전공", semiconductorIctCollege),
                        new Department("전기공학전공", semiconductorIctCollege),
                        new Department("전자공학전공", semiconductorIctCollege),
                        new Department("반도체시스템공학과", semiconductorIctCollege),
                        new Department("산업경영공학과", semiconductorIctCollege),
                        new Department("화학공학전공", engineeringCollege),
                        new Department("신소재공학전공", engineeringCollege),
                        new Department("환경시스템공학전공", engineeringCollege),
                        new Department("건설환경공학전공", engineeringCollege),
                        new Department("스마트모빌리티공학전공", engineeringCollege),
                        new Department("글로벌스마트인프라공학전공", engineeringCollege),
                        new Department("기계공학전공", engineeringCollege),
                        new Department("로봇공학전공", engineeringCollege),
                        new Department("화학나노학전공", engineeringCollege),
                        new Department("융합에너지학전공", engineeringCollege),
                        new Department("식품영양학전공", engineeringCollege),
                        new Department("시스템생명과학전공", engineeringCollege),
                        new Department("물리학과", engineeringCollege),
                        new Department("수학과", engineeringCollege),
                        new Department("비주얼커뮤니케이션디자인전공", engineeringCollege),
                        new Department("인더스트리얼디자인전공", engineeringCollege),
                        new Department("영상애니메이션디자인전공", engineeringCollege),
                        new Department("패션디자인전공", engineeringCollege),
                        new Department("체육학전공", engineeringCollege),
                        new Department("스포츠산업학전공", engineeringCollege),
                        new Department("스포츠지도학전공", engineeringCollege),
                        new Department("건반음악전공", engineeringCollege),
                        new Department("보컬뮤직전공", engineeringCollege),
                        new Department("작곡전공", engineeringCollege),
                        new Department("연극·영화전공", engineeringCollege),
                        new Department("뮤지컬공연전공", engineeringCollege),
                        new Department("바둑학과", engineeringCollege),
                        new Department("건축학전공", engineeringCollege),
                        new Department("전통건축전공", engineeringCollege),
                        new Department("공간디자인학과", engineeringCollege),
                        new Department("응용소프트웨어전공", engineeringCollege),
                        new Department("데이터사이언스전공", engineeringCollege),
                        new Department("인공지능전공", engineeringCollege),
                        new Department("디지털콘텐츠디자인학과", engineeringCollege),
                        new Department("중어중문학전공", humanitiesCollege),
                        new Department("일어일문학전공", humanitiesCollege),
                        new Department("아랍지역학전공", humanitiesCollege),
                        new Department("글로벌한국어학전공", humanitiesCollege),
                        new Department("국어국문학전공", humanitiesCollege),
                        new Department("영어영문학전공", humanitiesCollege),
                        new Department("미술사·역사학전공", humanitiesCollege),
                        new Department("문헌정보학전공", humanitiesCollege),
                        new Department("글로벌문화콘텐츠학전공", humanitiesCollege),
                        new Department("문예창작학과", humanitiesCollege),
                        new Department("행정학전공", humanitiesCollege),
                        new Department("정치외교학전공", humanitiesCollege),
                        new Department("경제학전공", humanitiesCollege),
                        new Department("국제통상학전공", humanitiesCollege),
                        new Department("응용통계학전공", humanitiesCollege),
                        new Department("법학과", humanitiesCollege),
                        new Department("경영학전공", businessCollege),
                        new Department("글로벌비즈니스학전공", businessCollege),
                        new Department("경영정보학과", businessCollege),
                        new Department("국제통상학과", businessCollege),
                        new Department("청소년지도학전공", humanitiesCollege),
                        new Department("아동학전공", humanitiesCollege),
                        new Department("사회복지학과", humanitiesCollege),
                        new Department("부동산학과", humanitiesCollege),
                        new Department("법무행정학과", humanitiesCollege),
                        new Department("심리치료학과", humanitiesCollege),
                        new Department("미래융합경영학과", humanitiesCollege),
                        new Department("멀티디자인학과", humanitiesCollege),
                        new Department("회계세무학과", humanitiesCollege),
                        new Department("계약학과", humanitiesCollege),
                        new Department("자율전공학부(인문)", humanitiesCollege),
                        new Department("자율전공학부(자연)", engineeringCollege)
                )
        );
    }
}
