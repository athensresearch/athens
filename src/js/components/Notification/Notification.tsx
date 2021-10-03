import { toast } from "react-hot-toast";
import styled from "styled-components";

const Wrap = styled.div`
  box-shadow: 0 0 1px rgba(0, 0, 0, 0.1);
  background: var(--background-color);
  padding: 1rem;
  border-radius: 1rem;
  opacity: 0;
  transition: all 0.3s ease-out;

  &.visible {
    opacity: 1;
  }

  &.toast-success {

  }
`;

export const NotificationItem = (props) => {
  const { toastItem, toast } = props;
  console.log(toast);
  return (
    <Wrap
      className={`toast-${toastItem.type} ${toastItem.visible ? "visible" : ""}`}
      style={{ opacity: toastItem.visible ? 1 : 0 }}
      {...toastItem}
    >
      <button onClick={() => toast.dismiss(toastItem.id)}>x</button>
    </Wrap>
  );
};

export const Notification = (props) => {
  const { children } = props;

  return toast.custom((t) => (
    <>
      <NotificationItem toast={t}>{children}</NotificationItem>
    </>
  ));
};
