import toast from "react-hot-toast";

import { NotificationContainer } from './components/NotificationContainer';
import { NotificationItem } from './components/NotificationItem';

const notify = toast;

// TODO: Properly extend Toast type with new options:
//   id: string;
//   isDismissable?: boolean;
//   onUndo?: () => void;
//   icon?: never;
//   iconTheme?: never;
//   undoMessage?: string;

export type Notification = any;

export { notify, NotificationContainer, NotificationItem };
