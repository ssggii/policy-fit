/**
 * F-005: 추정 고지 상시 노출. ResultView가 verdict.state 3개(가능/부적합/추가 확인 필요)
 * 전부에서 조건 없이 이 컴포넌트를 렌더링한다 — 이 컴포넌트 자체는 분기 없이 항상 같은 문구를 낸다.
 */
const STYLES = {
  container: "rounded border border-zinc-200 bg-zinc-50 px-4 py-3 text-sm text-zinc-600",
};

export default function Disclaimer() {
  return (
    <p className={STYLES.container}>
      이 결과는 입력하신 내용을 바탕으로 한 추정 판정입니다. 확정 여부는 반드시 공식 신청 절차에서 확인해주세요.
    </p>
  );
}
