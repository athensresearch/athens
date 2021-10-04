import {
  Toaster,
  ToastPosition,
  resolveValue,
} from "react-hot-toast";
import { NotificationItem } from '../components/NotificationItem';

const ToasterProps = {
  position: "bottom-right" as ToastPosition,
  containerStyle: {
    filter: "drop-shadow(0 0.5rem 0.5rem var(--shadow-color---opacity-25)"
  },
  gutter: 8, // 1rem
};

export const NotificationContainer = () => {
  return (
    <Toaster {...ToasterProps}>
      {(t) => <NotificationItem {...t}>{resolveValue(t.message, t)}</NotificationItem>}
    </Toaster>
  );
};
