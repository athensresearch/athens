import React from 'react';
import { BADGE, Storybook } from '../../../storybook';
import { blockTree } from '../Block/mockData';

import { Preview } from './Preview';
import { Link } from '../../Link';
import { renderBlocks } from '../Block/utils/renderBlocks';

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
    <Link
      ref={setAnchorEl}
      onMouseOver={() => setIsPreviewOpen(true)}
      onMouseOut={() => setIsPreviewOpen(false)}
      href="#"
    >
      link
    </Link>
    <Preview
      anchorEl={anchorEl}
      isPreviewOpen={isPreviewOpen}
    >
      <Preview.Media src="https://source.unsplash.com/random/400x400" />
      <Preview.Body>{(
        renderBlocks({
          tree: blockTree.tree,
          content: blockTree.blocks,
          lengthLimit: 3,
          blockComponent: <Preview.MiniBlock />
        })
      )}</Preview.Body>
    </Preview>
  </>)
}