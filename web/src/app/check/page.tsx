import CheckFlow from "@/components/check/CheckFlow";

/**
 * 체크플로우 페이지 셸. 화면마다 실제 콘텐츠 폭(정책 선택 880 / 질문·결과 520~560)이 달라
 * 이 페이지 자체엔 폭을 고정하지 않고, 각 화면(CheckFlow 내부)이 스스로 mx-auto로 중앙 정렬한다.
 */
export default function CheckPage() {
  return (
    <main className="flex w-full flex-1 flex-col gap-8 px-6 py-10 lg:py-16">
      <div className="mx-auto flex w-full max-w-[880px] items-center gap-2 text-[15px] font-extrabold tracking-tight text-ink">
        <span className="h-[18px] w-[18px] flex-none rounded-[6px] bg-blue" aria-hidden />
        폴핏
      </div>
      <CheckFlow />
    </main>
  );
}
