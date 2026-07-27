import CheckFlow from "@/components/check/CheckFlow";

export default function CheckPage() {
  return (
    <main className="mx-auto flex w-full max-w-xl flex-1 flex-col gap-6 px-6 py-12">
      <h1 className="text-xl font-semibold">청년 주택드림 청약통장 체크</h1>
      <CheckFlow />
    </main>
  );
}
