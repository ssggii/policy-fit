import Link from "next/link";

/**
 * 랜딩 페이지. 디자인 소스: docs/design/polfit-design.html의 "랜딩(진입)" 화면.
 * 모바일 우선(세로 배지·리스트) → lg에서 가운데 정렬 컬럼(스텝 3열 그리드)으로 넓어진다.
 */
const STEPS = [
  { title: "정책 고르기", desc: "궁금한 청년 정책을 선택해요." },
  { title: "질문에 답하기", desc: "나이·소득 등 몇 가지를 일상어로 답해요." },
  { title: "판정 받기", desc: "가능·부적합·추가확인·자가판정불가와 근거를 받아요." },
] as const;

const BADGES = ["인증 없이", "저장 안 함", "약 1분"] as const;

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-[520px] flex-1 flex-col gap-10 px-6 py-12 lg:max-w-3xl lg:items-center lg:py-20 lg:text-center">
      <div className="flex items-center gap-2 self-start text-[15px] font-extrabold tracking-tight text-ink lg:self-center">
        <span className="h-[18px] w-[18px] flex-none rounded-[6px] bg-blue" aria-hidden />
        폴핏
      </div>

      <div className="flex flex-col gap-4 lg:items-center">
        <h1 className="text-[28px] font-extrabold leading-[1.15] tracking-[-0.04em] lg:text-[40px]">
          복잡한 자격 요건,
          <br />
          1분 만에 판정
        </h1>
        <p className="max-w-[34ch] text-[13.5px] leading-relaxed text-muted lg:text-[18px] lg:leading-relaxed lg:text-ink/80">
          공동인증서도, 로그인도 없이. 몇 가지 질문에 답하면 지금 신청 가능한 청년 정책인지 바로 알려드려요.
        </p>
      </div>

      <div className="flex flex-wrap gap-2 lg:justify-center">
        {BADGES.map((label) => (
          <span
            key={label}
            className="inline-flex items-center gap-1.5 rounded-full bg-blue-wash px-3 py-1.5 text-xs font-bold text-blue"
          >
            <span className="h-1.5 w-1.5 flex-none rounded-full bg-blue" aria-hidden />
            {label}
          </span>
        ))}
      </div>

      <Link
        href="/check"
        className="transition-app inline-flex w-full items-center justify-center gap-2 rounded-input bg-blue px-5 py-4 text-base font-bold text-on-blue hover:bg-blue-press lg:w-auto lg:px-8 lg:text-[17px]"
      >
        체크 시작하기 →
      </Link>

      <div className="flex flex-col gap-3">
        <p className="text-xs font-bold uppercase tracking-[0.12em] text-muted lg:text-center">이렇게 진행돼요</p>
        <div className="grid grid-cols-1 gap-3 lg:grid-cols-3 lg:gap-4">
          {STEPS.map((step, i) => (
            <div key={step.title} className="flex flex-col gap-2 rounded-2xl border border-line bg-surface p-4 lg:text-left">
              <span className="grid h-7 w-7 flex-none place-items-center rounded-chip bg-blue-wash font-mono text-sm font-bold text-blue">
                {i + 1}
              </span>
              <p className="text-sm font-extrabold tracking-tight text-ink">{step.title}</p>
              <p className="text-xs leading-relaxed text-muted">{step.desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* F-005: 결과 화면뿐 아니라 진입 시점에도 추정 판정이라는 점을 미리 알린다.
          필수 고지라 장식성 faint(대비 ~3:1, AA 미달) 대신 muted를 쓴다. */}
      <p className="flex items-start gap-1.5 text-[11.5px] leading-relaxed text-muted lg:justify-center">
        <span className="font-extrabold text-muted" aria-hidden>
          ⓘ
        </span>
        <span>추정 판정이에요 — 확정은 공식 신청 절차에서 확인해주세요. 입력값은 저장하지 않아요.</span>
      </p>
    </main>
  );
}
