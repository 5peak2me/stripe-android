#!/usr/bin/env ruby

def replace_in_file(filename, pattern, replacement)
  puts "> Updating #{filename}"
  content = File.read(filename)
  new_content = content.sub(pattern, replacement)
  File.write(filename, new_content)
  execute_or_fail("git add #{filename}")
end

def update_read_me()
  replace_in_file("README.md",
      /implementation 'com.stripe:stripe-android:[.\d]+'/,
      "implementation 'com.stripe:stripe-android:#{@version}'",
  )
 end

def update_stripe_sdk_version()
  replace_in_file("stripe-core/src/main/java/com/stripe/android/core/version/StripeSdkVersion.kt",
      /const val VERSION_NAME = "[.\d]+"/,
      %Q{const val VERSION_NAME = "#{@version}"},
  )
end

def update_gradle_properties()
  replace_in_file("gradle.properties",
      /VERSION_NAME=[.\d]+/,
      "VERSION_NAME=#{@version}",
  )
end

def update_changelog()
    date = Time.now.strftime("%Y-%m-%d")

    replace_in_file(
        "CHANGELOG.md",
        /## XX.XX.XX - 20XX-XX-XX/,
        "## XX.XX.XX - 20XX-XX-XX\n\n## #{@version} - #{date}"
    )
end
