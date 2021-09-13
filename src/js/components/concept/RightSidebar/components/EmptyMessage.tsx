import styled from "styled-components";
import { VerticalSplit } from "@material-ui/icons";

const Message = styled.div`
  align-self: center;
  flex-direction: column;
  align-items: center;
  text-align: center;
  font-size: 80%;
  border-radius: 0.5rem;
  line-height: 1.3;
  margin: auto auto;
  display: flex;
  color: var(--body-text-color---opacity-med);

  svg {
    opacity: var(--opacity-low);
    font-size: 1000%;
  }

  kbd {
    border: 1px solid;
    text-transform: uppercase;
    border-radius: 0.2em;
    padding-left: 0.1em;
    padding-right: 0.1em;
  }
  
  p {
    max-width: 13em;
  }
`;

export const EmptyMessage = () => <Message>
  <VerticalSplit />
  <p>Hold <kbd>shift</kbd> when clicking a page link to view the page in the sidebar.</p>
</Message>;
