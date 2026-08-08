/**
 * F-005: 추정 고지 상시 노출. ResultView가 verdict.state 4개(가능/부적합/추가 확인 필요/자가판정 불가)
 * 전부에서 조건 없이 이 컴포넌트를 렌더링한다 — 이 컴포넌트 자체는 분기 없이 항상 같은 문구를 낸다.
 * 디자인: docs/design/polfit-design.html의 .honesty — 배경 없이, 아이콘 + 옅은 텍스트로만 표시한다
 * (verdict-hero 안에 놓이므로 상태색 배경 위에 얹힌다).
 */
const STYLES = {
  // F-005 필수 고지 — 장식성 faint(대비 ~3:1, AA 미달) 대신 muted를 쓴다.
  container: "flex items-start gap-1.5 text-[11.5px] leading-relaxed text-muted",
  icon: "flex-none font-extrabold text-muted",
};

export default function Disclaimer() {
  return (
    <p className={STYLES.container}>
      <span className={STYLES.icon} aria-hidden>
        ⓘ
      </span>
      <span>이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다. 확정 여부는 반드시 공식 신청 절차에서 확인해주세요.</span>
    </p>
  );
}
