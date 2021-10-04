import toast, {
  Toast,
} from "react-hot-toast";

import { NotificationContainer } from './components/NotificationContainer';
import { NotificationItem } from './components/NotificationItem';

const notify = toast;

export interface Notification
  extends Toast,
  Partial<React.HTMLAttributes<HTMLElement>> {
  id: string;
  isDismissable?: boolean;
  onUndo?: () => void;
  icon?: never;
  iconTheme?: never;
  undoMessage?: string;
}

export { notify, NotificationContainer, NotificationItem };
