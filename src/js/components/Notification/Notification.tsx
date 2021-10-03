import { toast } from "react-hot-toast";
import styled from "styled-components";

const Wrap = styled.div`
  border: 4px solid green;
`;

const NotificationItem = (props) => {
  const { toast } = props;
  return (
    <Wrap style={{ opacity: toast.visible ? 1 : 0 }}>
      <h1>props.children</h1>
    </Wrap>
  );
};

export const Notification = (props) => {
  const { children } = props;

  return toast.custom((t) => (
    <NotificationItem toast={t}>{children}</NotificationItem>
  ));
};
