import { Storybook } from "@/utils/storybook";

import { notify } from "@/Notifications/Notifications";
import { Button } from "@/Button";
import { Indeterminate } from "@/Spinner/components/Indeterminate";

export default {
  title: "Components/Notification",
  component: Notification,
  argTypes: {},
  parameters: {
    layout: "centered",
    decorators: [
      (Story, args) => (
        <Storybook.Wrapper>
          <Story {...args} />
        </Storybook.Wrapper>
      ),
    ],
  },
};

export const Active = () => {
  return (
    <div style={{ display: "flex", gap: "0.5rem", flexDirection: "column" }}>
      <Button
        variant="tinted"
        onClick={() =>
          notify("You did something you might not have meant to", {
            id: "undoable-0",
            onUndo: () => console.log('undid action')
          })
        }
      >
        Undoable Action
      </Button>
      <Button
        variant="tinted"
        onClick={() =>
          notify.success("Ping", {
            id: "undoable-1",
            duration: Infinity,
            isDismissable: true,
            undoMessage: 'Undid the thing',
            onUndo: () => console.log('undid action')
          })
        }
      >
        Undoable Success Action
      </Button>
      <Button
        variant="tinted"
        onClick={() =>
          notify.loading(
            <>
              <Indeterminate style={{ "--size": "1.5rem" }} /> Loading...
            </>,
            {
              id: "loading",
              position: "bottom-center",
            }
          )
        }
      >
        Loading
      </Button>
      <Button
        variant="tinted"
        onClick={() =>
          notify.error("Error! Something went wrong", { isDismissable: true })
        }
      >
        Dismissable Error
      </Button>
      <Button
        variant="tinted"
        onClick={() =>
          notify.error("Error! Something went wrong", {
            duration: Infinity,
            isDismissable: true,
            position: "top-left",
          })
        }
      >
        Custom Position
      </Button>
      <Button
        variant="tinted"
        onClick={() => notify("Nothing happened")}
      >
        Plain
      </Button>
      <hr />
      <Button variant="tinted" onClick={() => notify.dismiss()}>
        Clear all
      </Button>
    </div>
  );
};
