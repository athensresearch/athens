# Table of Contents

1. [A Self-Hosted Athens](#a-self-hosted-athens)
1. [An Individual Memex](#an-individual-memex)
1. [A Collective Memex](#a-collective-memex)
   - [A Protocol for Bi-directionality](#a-protocol-for-bi-directionality)
   - [A Protocol for Knowledge Markets](#a-protocol-for-knowledge-markets)
1. [A Community for Collaboration and Learning](#a-community-for-collaboration-and-learning)

---

> There is nothing impossible to him (or her) who will try.

— Alexander the Great

This document can be read alongside [Athens Vision Mindmap v2](https://www.notion.so/athensresearch/Athens-Roadmap-Mindmap-v2-096427f189b648729ae0acbdcefd4c6f?showMoveTo=true).

# A Self-Hosted Athens

We've recently seen an explosion in Tools for *Networked* Thought like [Roam Research](http://roamresearch.com/), [Obsidian](https://obsidian.md/), [Semilattice](https://www.semilattice.xyz/), and [so on](https://www.notion.so/Networked-Note-taking-app-a131b468fc6f43218fb8105430304709) and [so forth](https://twitter.com/patrick_oshag/status/1264299702738173954?s=20). These tools create value because they allow users to create knowledge associatively, divergently, and then emergently.

This is more important now than ever before because in the past few decades we've seen exponential growth in data and information. However, our ability to make sense of these inputs, to convert data and information to knowledge and wisdom, is already capped by the mainstream tools today.

Using a graph database, bi-directional links, and hypertext transclusions, these tools enable users to create and traverse knowledge in a way that mirrors how humans organically create knowledge — associatively and contextually.

These "knowledge graphs" break out of the "file-and-cabinet" hierarchical paradigm that most computer systems use — note-taking apps of the last few decades,  filesystems, HTML documents, etc. Users using Tools for Networked Thought can more easily create meaningful relationships about the world we live in. Not only that, these users can then more easily recontextualize relationships, allowing ideas to compose and refactor in emergent ways. This leads to the creation of more insights, more interdisciplinary insights, and generative insights: insights that create more interdisciplinary insights.

In short, we are currently experiencing diminishing and marginal returns on information. Tools for Networked Thought promise exponential and compound returns.

Those are the other Tools for Networked Thought. But this is Athens. So why Athens, why open-source? Because Tools for Networked Thought should be open-source.

Many users already report that they are getting massive returns on their knowledge graphs. They've found perhaps the currently closest approximation to [Vannevar Bush's memex](https://www.theatlantic.com/magazine/archive/1945/07/as-we-may-think/303881/). For some of these tools, users right now are putting their entire personal lives, unencrypted, in a public cloud.

This isn't about any specific company's ethics, either. This is about users putting their second brains — their entire lives — on a public cloud. In plain-text. To be summoned at will by the government ([Facebook Transparency](https://transparency.facebook.com/government-data-requests/country/US), [Google Transparency](https://transparencyreport.google.com/user-data/overview?hl=en)).

A self-hosted, open-source Athens solves this privacy and security problem.

# An Individual Memex

The closest approximations we have to the brain are Tools for Networked Thought and neural networks. Both of these are graph networks with bi-directional data flows.

What makes Tools for *Networked* Thought different from normal Tools for Thought is that they have bi-directional links and transclusions (which are just richer bi-directional links).

Of course, it's not *just* bi-directional links. Any note-taking app can support bi-directional links with some string parsing. It's also the fact that many of these tools are built on top of a graph database.

To acquire exponential returns on knowledge, we will need to be able to navigate and manipulate exponentially large datasets. Datasets that approach Wikipedia and Google scale.

We will need specific features like graph visualizations as well as an industrial data query language (Datalog) that can represent and operate on thousands if not millions of nodes, blocks, and edges.

Knowledge at scale requires a data model that is more robust than a collection of markdown files.

Athens has implemented bi-directional links and transclusions. This can be and has been done in under 100 lines of Clojure and Datascript.

Everything else — contextual panes and previews, queries on your graph database, the graph visualization itself — are green fields. There are no established best practice for how to interact with a knowledge graph, let alone represent it.

- How might tables and queries in Athens be as powerful as tables in Airtable, Notion, or Excel?
- How might we more fully leverage our spatial and visual senses with a dynamic and interactive knowledge graph? See [interactive graph visualization (#21)](https://github.com/athensresearch/athens/issues/21).
- What if Athens was as extensible as Emacs and Vim? As interactive as Smalltalk and LightTable? And most importantly, as easy as Myspace? See [plugin architecture (#63)](https://github.com/athensresearch/athens/issues/63).

# A Collective Memex

The closest approximation we have to a global brain is the Web. But even still, the Web is far from a collective memex.

As already mentioned, a memex is probably a bi-directional graph, whereas the Web is only uni-directional.

This means the Web needs bi-directionality within more apps. And then it needs bi-directionality between apps.

## A Protocol for Bi-directionality

The Web should've always have been bi-directional. The reason why it's not is because it's a security nightmare. Letting someone write to your website or server by simply linking to your domain necessarily opens you up to malicious exploits. There have been a [few unsuccessful attempts at notifications](https://en.wikipedia.org/wiki/Linkback) when someone links to your blog. Even these have led to [DDoS attacks](https://en.wikipedia.org/wiki/Pingback).

We need a chat messenger like the one [Max Krieger](http://a9.io/glue-comic/) envisions. Chat is one of the clearest examples of diminishing returns on knowledge. If you don't check Slack for one day, you'll never catch up. Even if you do, good luck finding it again. Furthermore, aside from these diminishing returns on data, conversations are naturally networked and self-referential. But all we have right now is a ceaseless tidal wave of messages represented as an append-only log. Managing it all with search, one level of threads, and channels is Sisyphean at best.

We also need an IDE that can seamlessly integrate specs, documentation, and code. When we only look at source code, we lose out on so much: previous versions the code evolved from, alternative implementations on different branches and forks, and the conversation between developers happening on issues and PRs. [Unison](https://www.unisonweb.org/docs/tour), [darklang](https://darklang.com/), [repl.it](https://repl.it/), and [ObservableHQ](http://observablehq.com/)  are showing us the power of tooling and languages that treat the Web as a first-class citizen. With WASM and GraalVM on the horizon, soon we'll be able to write and run any code anywhere. We should have IDEs and languages that are similarly flexible, that can exploit modern UIs and distributed backends.

This is a lot to ask out of any one app. That's why we have specific apps for specific purposes. The point here is that while a lot can be done using bi-directional links within an app, far more could be done using bi-directional links between apps. [Apps today are siloed](https://uxdesign.cc/introducing-mercury-os-f4de45a04289). A protocol that enables apps to securely communicate data between one another would break down these siloes. This would give you, the end-user, vastly more power.

Bi-directional links aren't necessary for everything, they're not going to save the world (although they might; see next section). But many of these apps are just UIs over your personal data. You should have greater control and power over your data.

## A Protocol for Knowledge Markets

The closest approximation we have to a global brain is the ~~Web~~ Market. Federal banks aside, the Market does a pretty good job of finding supply and demand equilibria for 7.6 billion agents all with different preferences on the price, volume, and quality of different goods and services.

But the Efficient Market hypothesis seems to be failing in a few places: healthcare, education, and knowledge. Athens is directly focused on knowledge. What is the problem here?

> There is currently little accountability for predictions. Politicians make baseless predictions with no accountability, while the media profits from sensationalist journalism. Pundits of all stripes have no skin in the game. Even when they get things wrong, they typically don’t go back to correct themselves. Experts don’t have incentives to speak up. Too much to lose.

> Charlatans, however, make baseless predictions to build an audience. If they’re wrong, their tribe still supports them. Celebrities are winning The War of Ideas. Tribalism above truth. Entertainment over everything.

— Erik Torenberg, "[A Primer On Prediction Markets](https://www.tokendaily.co/blog/a-primer-on-prediction-markets)"

What, if instead, we had what Mike Elias describes as:

```
- A protocol for the battlefield of ideas
- A protocol for trustless credibility
- A protocol for defining reality without media corporations
- A protocol for harnessing greed to empower curiosity
- A protocol for capturing the value of obscure genius
- A protocol for perpetual scientific and cultural revolution
- A protocol for killing bad ideas
- A protocol for rendering propaganda powerless
- A protocol for making credibility harder to achieve by force than by merit
- A protocol for using price discovery to advance discovery
- A protocol for creating high-quality common knowledge
- A perpetual global dashboard for sincere belief
- An authority without authorities
- An intellectual gold mine
```

— Mike Elias, "[Decentralizing the search for truth using idea markets](https://medium.com/@harmonylion1/decentralizing-the-search-for-truth-using-financial-markets-648bf4408b5c)"

It's unclear whether prediction markets, idea markets, or ultimately knowledge markets will solve the epistemic crisis we're currently in.

It's unlikely that just a protocol will be the silver bullet.

What is clear is that the current platforms (Facebook and Google) and knowledge institutions (media corporations, governments, and universities) don't appear to be up to the job.

What is likely is that knowledge graphs are here to stay.

That multiplayer, joinable knowledge graphs will follow.

And that we need to start getting exponential returns on collective intelligence.

# A Community for Collaboration and Learning

Lastly, the final Athens vision is for everyone, one day, to learn how to learn anything.

To do that, it is mission-critical to create a space and culture where we can [work with one another and learn from one another](https://github.com/athensresearch/athens/blob/master/CODE_OF_CONDUCT.md).

That is the meta-project. Memexes, protocols, and Athens are just a means of getting there.
