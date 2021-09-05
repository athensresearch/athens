import React from 'react';
import { BADGE, Storybook } from '../../storybook';

import { Preview } from './Preview';

export default {
  title: 'concepts/Preview',
  component: Preview,
  argTypes: {},
  parameters: {
    layout: 'fullscreen',
    badges: [BADGE.CONCEPT]
  },
  decorators: [(Story) => <Storybook.Wrapper style={{ gap: "2rem" }}><Story /></Storybook.Wrapper>]

};

export const Basic = () => {
  const [isPreviewOpen, setIsPreviewOpen] = React.useState(false);
  const [anchorEl, setAnchorEl] = React.useState(null);

  return (<>
    <a
      ref={setAnchorEl}
      onMouseOver={() => setIsPreviewOpen(true)}
      onMouseOut={() => setIsPreviewOpen(false)}
      href="#"
    >
      link
    </a>
    <Preview
      anchorEl={anchorEl}
      isPreviewOpen={isPreviewOpen}
    >
      <Preview.Media src="https://source.unsplash.com/random/400x400" />
      <Preview.Title>Link</Preview.Title>
      <Preview.Body>this is some preview content</Preview.Body>
    </Preview>
  </>)
}