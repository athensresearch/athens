import { Storybook } from "@/utils/storybook";
import { Notification, NotificationItem } from "./Notification";

import toast from "react-hot-toast";
import { Button } from "@/Button";

export default {
  title: "Components/Notification",
  component: Notification,
  argTypes: {},
  parameters: {
    layout: "centered",
  },
};

const Template = (Story, args) => (
  <Storybook.Wrapper>
    <Story {...args} />
  </Storybook.Wrapper>
);

export const Basic = Template.bind({});
Basic.args = {
  children: "Spinner",
};

export const Active = () => {
  return (
    <>
      <Button
        variant="filled"
        shape="round"
        onClick={() =>
          toast.custom((t) => (
            <NotificationItem
              toastItem={t}
              toast={toast}

            >
              content
            </NotificationItem>
          ))
        }
      >
        Alert
      </Button>
    </>
  );
};
