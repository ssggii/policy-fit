import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PolicySelect from "./PolicySelect";
import { POLICIES } from "./policies";

describe("PolicySelect", () => {
  it("MVP 3정책을 모두 목록으로 보여준다", () => {
    render(<PolicySelect onSelect={vi.fn()} />);
    for (const policy of POLICIES) {
      expect(screen.getByRole("button", { name: new RegExp(policy.name) })).toBeInTheDocument();
    }
  });

  it("각 정책을 고르면 해당 policy_id로 onSelect가 호출된다", async () => {
    for (const policy of POLICIES) {
      const onSelect = vi.fn();
      const user = userEvent.setup();
      const { unmount } = render(<PolicySelect onSelect={onSelect} />);
      await user.click(screen.getByRole("button", { name: new RegExp(policy.name) }));
      expect(onSelect).toHaveBeenCalledWith(policy.id);
      unmount();
    }
  });
});
